# HACKATHON MVP - Complete Backend in One File
# Run: pip install fastapi uvicorn pydantic
# Start: uvicorn hackathon_backend:app --reload

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, List, Optional
from datetime import datetime, timedelta
from decimal import Decimal
import hashlib
import json
import uuid

app = FastAPI(title="Green Coin API")

# Enable CORS for frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# IN-MEMORY DATABASE (perfect for hackathon demo)
users_db = {}
wallets_db = {}
actions_db = []
transactions_db = []
leaderboard_cache = []

# CONSTANTS
SPLIT_RATIO = (1, 49)  # 1 money, 49 points
COIN_TO_USD = 0.10
CO2_PER_TAB = 0.005  # kg
CO2_PER_KM_EFFICIENT = 0.05  # kg
CO2_PER_KM_INEFFICIENT = 0.15  # kg

# MODELS
class User(BaseModel):
    username: str
    email: str

class Action(BaseModel):
    user_id: str
    action_type: str  # 'close_tabs', 'efficient_drive', 'ai_optimize'
    metadata: Dict  # {tabs: 10} or {distance_km: 5, efficiency: 'good'}

class Redemption(BaseModel):
    user_id: str
    wallet_type: str  # 'money' or 'points'
    amount: float
    redemption_type: str  # 'cash', 'gift_card', 'carbon_offset'

# HELPER FUNCTIONS
def calculate_co2_saved(action_type: str, metadata: Dict) -> float:
    """Calculate CO2 saved based on action"""
    if action_type == 'close_tabs':
        return metadata.get('tabs', 1) * CO2_PER_TAB
    
    elif action_type == 'efficient_drive':
        distance = metadata.get('distance_km', 0)
        efficiency = metadata.get('efficiency', 'medium')
        
        if efficiency == 'good':
            return distance * (CO2_PER_KM_INEFFICIENT - CO2_PER_KM_EFFICIENT)
        else:
            return distance * 0.05  # Baseline savings
    
    elif action_type == 'ai_optimize':
        prompts = metadata.get('prompts_optimized', 1)
        return prompts * 0.01  # 10g per optimized prompt
    
    elif action_type == 'server_optimize':
        return metadata.get('kwh_saved', 1) * 0.5  # 0.5kg CO2 per kWh
    
    return 0.01  # Default minimal credit

def calculate_streak_multiplier(streak_days: int) -> float:
    """Calculate coin multiplier based on streak"""
    if streak_days < 7:
        return 1.0
    elif streak_days < 30:
        return 1.5
    elif streak_days < 90:
        return 2.0
    else:
        return 2.5

def get_or_create_user(user_id: str):
    """Get user or create if doesn't exist"""
    if user_id not in users_db:
        users_db[user_id] = {
            'user_id': user_id,
            'username': f'User{len(users_db) + 1}',
            'email': f'user{len(users_db) + 1}@greencoin.app',
            'total_co2_saved_kg': 0,
            'current_streak_days': 0,
            'last_action_date': None,
            'created_at': datetime.utcnow().isoformat()
        }
        
        # Create dual wallets
        wallets_db[f"{user_id}_money"] = {
            'user_id': user_id,
            'wallet_type': 'money',
            'balance': 0
        }
        wallets_db[f"{user_id}_points"] = {
            'user_id': user_id,
            'wallet_type': 'points',
            'balance': 0
        }
    
    return users_db[user_id]

def update_streak(user_id: str):
    """Update user's streak"""
    user = users_db[user_id]
    today = datetime.utcnow().date()
    
    if user['last_action_date'] is None:
        user['current_streak_days'] = 1
    else:
        last_date = datetime.fromisoformat(user['last_action_date']).date()
        days_diff = (today - last_date).days
        
        if days_diff == 1:
            user['current_streak_days'] += 1
        elif days_diff == 0:
            pass  # Same day, keep streak
        else:
            user['current_streak_days'] = 1  # Streak broken
    
    user['last_action_date'] = datetime.utcnow().isoformat()
    return user['current_streak_days']

# API ENDPOINTS

@app.get("/")
def read_root():
    return {
        "message": "Green Coin API - Hackathon MVP",
        "endpoints": {
            "POST /register": "Register user",
            "POST /action": "Log eco-action and mint coins",
            "GET /balance/{user_id}": "Get wallet balances",
            "POST /redeem": "Redeem coins",
            "GET /leaderboard": "Top users by CO2 saved",
            "GET /stats": "Global statistics"
        }
    }

@app.post("/register")
def register_user(user: User):
    """Register new user"""
    user_id = str(uuid.uuid4())
    users_db[user_id] = {
        'user_id': user_id,
        'username': user.username,
        'email': user.email,
        'total_co2_saved_kg': 0,
        'current_streak_days': 0,
        'last_action_date': None,
        'created_at': datetime.utcnow().isoformat()
    }
    
    # Create wallets
    wallets_db[f"{user_id}_money"] = {
        'user_id': user_id,
        'wallet_type': 'money',
        'balance': 0
    }
    wallets_db[f"{user_id}_points"] = {
        'user_id': user_id,
        'wallet_type': 'points',
        'balance': 0
    }
    
    return {
        "user_id": user_id,
        "username": user.username,
        "message": "User registered successfully"
    }

@app.post("/action")
def log_action(action: Action):
    """User performs eco-action, system mints coins"""
    
    # Get or create user
    user = get_or_create_user(action.user_id)
    
    # Calculate CO2 saved
    co2_saved = calculate_co2_saved(action.action_type, action.metadata)
    
    # Update streak
    streak_days = update_streak(action.user_id)
    multiplier = calculate_streak_multiplier(streak_days)
    
    # Calculate coins (1 coin = 1 kg CO2)
    base_coins = co2_saved
    total_coins = base_coins * multiplier
    
    # Split into money and points
    money_portion = total_coins / 50
    points_portion = total_coins * 49 / 50
    
    # Update wallets
    wallets_db[f"{action.user_id}_money"]['balance'] += money_portion
    wallets_db[f"{action.user_id}_points"]['balance'] += points_portion
    
    # Update user stats
    user['total_co2_saved_kg'] += co2_saved
    
    # Log action
    action_record = {
        'action_id': str(uuid.uuid4()),
        'user_id': action.user_id,
        'action_type': action.action_type,
        'co2_saved_kg': co2_saved,
        'coins_minted': total_coins,
        'money_portion': money_portion,
        'points_portion': points_portion,
        'multiplier': multiplier,
        'streak_days': streak_days,
        'metadata': action.metadata,
        'timestamp': datetime.utcnow().isoformat()
    }
    actions_db.append(action_record)
    
    # Log transaction
    tx = {
        'tx_id': str(uuid.uuid4()),
        'user_id': action.user_id,
        'type': 'mint',
        'amount': total_coins,
        'timestamp': datetime.utcnow().isoformat()
    }
    transactions_db.append(tx)
    
    return {
        "success": True,
        "co2_saved_kg": round(co2_saved, 4),
        "coins_minted": round(total_coins, 4),
        "money_wallet": round(money_portion, 4),
        "points_wallet": round(points_portion, 4),
        "streak_days": streak_days,
        "multiplier": multiplier,
        "message": f"Great work! You saved {round(co2_saved, 3)} kg CO₂"
    }

@app.get("/balance/{user_id}")
def get_balance(user_id: str):
    """Get user's wallet balances and stats"""
    
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    user = users_db[user_id]
    money_wallet = wallets_db[f"{user_id}_money"]
    points_wallet = wallets_db[f"{user_id}_points"]
    
    # Calculate total USD value
    total_usd = (money_wallet['balance'] + points_wallet['balance']) * COIN_TO_USD
    
    return {
        "user_id": user_id,
        "username": user['username'],
        "wallets": {
            "money": {
                "balance_coins": round(money_wallet['balance'], 4),
                "balance_usd": round(money_wallet['balance'] * COIN_TO_USD, 2)
            },
            "points": {
                "balance_coins": round(points_wallet['balance'], 4),
                "balance_usd": round(points_wallet['balance'] * COIN_TO_USD * 0.9, 2)
            }
        },
        "stats": {
            "total_co2_saved_kg": round(user['total_co2_saved_kg'], 2),
            "current_streak_days": user['current_streak_days'],
            "total_value_usd": round(total_usd, 2),
            "actions_count": len([a for a in actions_db if a['user_id'] == user_id])
        }
    }

@app.post("/redeem")
def redeem_coins(redemption: Redemption):
    """User redeems coins for rewards"""
    
    user_id = redemption.user_id
    wallet_key = f"{user_id}_{redemption.wallet_type}"
    
    if wallet_key not in wallets_db:
        raise HTTPException(status_code=404, detail="Wallet not found")
    
    wallet = wallets_db[wallet_key]
    
    if wallet['balance'] < redemption.amount:
        raise HTTPException(status_code=400, detail="Insufficient balance")
    
    # Calculate value
    if redemption.wallet_type == 'money':
        usd_value = redemption.amount * COIN_TO_USD
    else:
        usd_value = redemption.amount * COIN_TO_USD * 0.9
    
    platform_fee = usd_value * 0.05
    user_receives = usd_value - platform_fee
    
    # Update wallet
    wallet['balance'] -= redemption.amount
    
    # Log transaction
    tx = {
        'tx_id': str(uuid.uuid4()),
        'user_id': user_id,
        'type': 'redeem',
        'wallet_type': redemption.wallet_type,
        'amount': redemption.amount,
        'usd_value': usd_value,
        'user_receives': user_receives,
        'platform_fee': platform_fee,
        'redemption_type': redemption.redemption_type,
        'timestamp': datetime.utcnow().isoformat()
    }
    transactions_db.append(tx)
    
    return {
        "success": True,
        "coins_redeemed": redemption.amount,
        "usd_value": round(usd_value, 2),
        "you_receive": round(user_receives, 2),
        "platform_fee": round(platform_fee, 2),
        "redemption_type": redemption.redemption_type,
        "new_balance": round(wallet['balance'], 4)
    }

@app.get("/leaderboard")
def get_leaderboard(limit: int = 10):
    """Get top users by CO2 saved"""
    
    sorted_users = sorted(
        users_db.values(),
        key=lambda x: x['total_co2_saved_kg'],
        reverse=True
    )[:limit]
    
    leaderboard = []
    for rank, user in enumerate(sorted_users, 1):
        money_balance = wallets_db[f"{user['user_id']}_money"]['balance']
        points_balance = wallets_db[f"{user['user_id']}_points"]['balance']
        
        leaderboard.append({
            'rank': rank,
            'username': user['username'],
            'co2_saved_kg': round(user['total_co2_saved_kg'], 2),
            'total_coins': round(money_balance + points_balance, 2),
            'streak_days': user['current_streak_days']
        })
    
    return {"leaderboard": leaderboard}

@app.get("/stats")
def get_global_stats():
    """Get platform-wide statistics"""
    
    total_users = len(users_db)
    total_co2_saved = sum(u['total_co2_saved_kg'] for u in users_db.values())
    total_actions = len(actions_db)
    total_coins_minted = sum(a['coins_minted'] for a in actions_db)
    
    # Action type breakdown
    action_types = {}
    for action in actions_db:
        atype = action['action_type']
        action_types[atype] = action_types.get(atype, 0) + 1
    
    return {
        "total_users": total_users,
        "total_co2_saved_kg": round(total_co2_saved, 2),
        "total_actions": total_actions,
        "total_coins_minted": round(total_coins_minted, 2),
        "total_value_usd": round(total_coins_minted * COIN_TO_USD, 2),
        "action_breakdown": action_types,
        "trees_equivalent": round(total_co2_saved / 20, 1)  # 1 tree absorbs ~20kg/year
    }

@app.get("/user/{user_id}/history")
def get_user_history(user_id: str, limit: int = 20):
    """Get user's recent actions"""
    
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_actions = [a for a in actions_db if a['user_id'] == user_id]
    user_actions.sort(key=lambda x: x['timestamp'], reverse=True)
    
    return {
        "user_id": user_id,
        "actions": user_actions[:limit]
    }

# DEMO DATA SEEDER (for testing)
@app.post("/seed-demo-data")
def seed_demo_data():
    """Create demo users and actions for presentation"""
    
    demo_users = [
        {"username": "Alice", "email": "alice@demo.com"},
        {"username": "Bob", "email": "bob@demo.com"},
        {"username": "Carol", "email": "carol@demo.com"}
    ]
    
    user_ids = []
    for user_data in demo_users:
        response = register_user(User(**user_data))
        user_ids.append(response['user_id'])
    
    # Generate demo actions
    demo_actions = [
        {"user_id": user_ids[0], "action_type": "close_tabs", "metadata": {"tabs": 15}},
        {"user_id": user_ids[0], "action_type": "efficient_drive", "metadata": {"distance_km": 10, "efficiency": "good"}},
        {"user_id": user_ids[1], "action_type": "ai_optimize", "metadata": {"prompts_optimized": 20}},
        {"user_id": user_ids[1], "action_type": "close_tabs", "metadata": {"tabs": 8}},
        {"user_id": user_ids[2], "action_type": "server_optimize", "metadata": {"kwh_saved": 5}},
    ]
    
    for action_data in demo_actions:
        log_action(Action(**action_data))
    
    return {
        "message": "Demo data seeded successfully",
        "users_created": len(user_ids),
        "actions_created": len(demo_actions)
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)