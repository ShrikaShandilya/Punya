#!/bin/bash

# GreenCoin Mining Simulator - Interactive Educational Version
# Inspired by Proof-of-Stake concepts

# Colors for better visualization
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Initialize variables
declare -A wallets  # Associate array to store wallet balances
declare -A stakes   # Associate array to store staked amounts
total_coins=1000000
num_validators=5
block_reward=50
energy_usage=0

press_enter() {
    echo -e "\n${YELLOW}Press Enter to continue...${NC}"
    read
}

explain() {
    echo -e "\n${CYAN}💡 $1${NC}"
    press_enter
}

# Initialize wallets with random balances
init_wallets() {
    explain "In a Proof-of-Stake system, participants (called validators) need to have coins to participate in block creation. Let's set up our validators with initial balances."
    
    echo -e "${BLUE}Initializing wallets...${NC}"
    for ((i=1; i<=num_validators; i++)); do
        initial_balance=$((RANDOM % 10000 + 5000))
        wallets["validator$i"]=$initial_balance
        stakes["validator$i"]=0
        echo "Validator $i wallet balance: ${wallets["validator$i"]} GREENCOIN"
    done
    
    explain "Each validator now has a random initial balance of GREENCOIN. These coins can be used for staking."
}

# Stake coins
stake_coins() {
    local validator=$1
    local amount=$2
    
    explain "Staking is the process of locking up coins as collateral. The more coins staked, the higher chance of being selected to create the next block."
    
    if [ ${wallets[$validator]} -ge $amount ]; then
        wallets[$validator]=$((${wallets[$validator]} - amount))
        stakes[$validator]=$((${stakes[$validator]} + amount))
        echo -e "${GREEN}$validator staked $amount GREENCOIN${NC}"
        echo "New wallet balance: ${wallets[$validator]} GREENCOIN"
        echo "Total staked: ${stakes[$validator]} GREENCOIN"
        return 0
    else
        echo -e "${YELLOW}Insufficient balance for staking${NC}"
        return 1
    fi
}

# Simulate block creation with Proof of Stake
create_block() {
    echo -e "\n${BLUE}Creating new block...${NC}"
    
    explain "In Proof-of-Stake, validators are chosen based on how many coins they've staked. This is like a lottery where more staked coins = more tickets."
    
    # Calculate total stake
    local total_stake=0
    echo -e "\n${BLUE}Calculating total staked coins in the network...${NC}"
    for validator in "${!stakes[@]}"; do
        total_stake=$((total_stake + ${stakes[$validator]}))
        echo "$validator has staked: ${stakes[$validator]} GREENCOIN"
    done
    echo "Total staked in network: $total_stake GREENCOIN"
    
    press_enter
    
    if [ $total_stake -eq 0 ]; then
        echo -e "${YELLOW}No stakes found. Cannot create block.${NC}"
        return
    fi
    
    # Random selection weighted by stake
    local random_point=$((RANDOM % total_stake))
    echo -e "\n${BLUE}Selecting validator based on stake weight...${NC}"
    echo "Random selection point: $random_point out of $total_stake total stake"
    
    local cumulative=0
    local selected_validator=""
    
    for validator in "${!stakes[@]}"; do
        cumulative=$((cumulative + ${stakes[$validator]}))
        echo "Checking $validator (cumulative stake: $cumulative)..."
        if [ $random_point -lt $cumulative ]; then
            selected_validator=$validator
            break
        fi
    done
    
    explain "The validator is selected based on their proportion of the total stake. Higher stake = higher chance of selection."
    
    # Reward the selected validator
    wallets[$selected_validator]=$((${wallets[$selected_validator]} + block_reward))
    energy_usage=$((energy_usage + 1)) # Minimal energy usage compared to PoW
    
    echo -e "${GREEN}Block created by $selected_validator${NC}"
    echo "Block reward: $block_reward GREENCOIN"
    echo "Energy used for this block: 1 unit"
    echo "Total energy used so far: $energy_usage units"
    
    explain "Unlike Proof-of-Work which uses massive computing power, Proof-of-Stake only needs minimal energy to validate transactions and create blocks."
}

# Display current state
show_status() {
    echo -e "\n${BLUE}Current Status:${NC}"
    echo "----------------------------------------"
    for validator in "${!wallets[@]}"; do
        echo "$validator:"
        echo "  Balance: ${wallets[$validator]} GREENCOIN"
        echo "  Staked: ${stakes[$validator]} GREENCOIN"
    done
    echo "----------------------------------------"
    echo "Total energy used: $energy_usage units"
    
    explain "This shows each validator's current balance and staked amount. Notice how balances change as rewards are distributed and energy usage stays low."
}

# Main simulation loop
main() {
    echo -e "${GREEN}Welcome to GreenCoin Mining Simulator - Educational Version${NC}"
    echo "-----------------------------------------------------"
    
    explain "This simulator demonstrates how a Proof-of-Stake cryptocurrency works. Unlike Bitcoin's energy-intensive Proof-of-Work, this system is much more environmentally friendly."
    
    init_wallets
    
    explain "Now that wallets are initialized, validators will stake some of their coins. This gives them the right to participate in block creation."
    
    # Initial staking
    for validator in "${!wallets[@]}"; do
        stake_amount=$((RANDOM % 1000 + 500))
        stake_coins $validator $stake_amount
    done
    
    # Simulation loop
    for ((round=1; round<=5; round++)); do
        echo -e "\n${BLUE}Round $round${NC}"
        echo "----------------"
        create_block
        show_status
    done
    
    explain "Simulation complete! Notice how blocks were created with minimal energy usage, and validators were rewarded based on their stake rather than computing power."
}

# Run the simulation
main