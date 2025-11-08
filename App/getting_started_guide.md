# 🚨 12-HOUR HACKATHON BUILD PLAN

## ⏰ EXACT TIMELINE

### **HOUR 1-2: Backend Setup** ✅ DONE
```bash
# 1. Create folder
mkdir green-coin-hackathon
cd green-coin-hackathon

# 2. Install Python dependencies
pip install fastapi uvicorn pydantic

# 3. Copy backend code (from artifact 2) into:
# hackathon_backend.py

# 4. Start server
uvicorn hackathon_backend:app --reload

# 5. Test in browser
# http://localhost:8000
# http://localhost:8000/docs (Swagger UI)
```

**✅ CHECKPOINT:** You should see API docs at `/docs`

---

### **HOUR 3-5: React Frontend** ✅ DONE
```bash
# 1. Create React app
npx create-react-app green-coin-frontend
cd green-coin-frontend

# 2. Install dependencies
npm install lucide-react

# 3. Replace src/App.js with artifact 3 code

# 4. Start frontend
npm start

# 5. Open browser
# http://localhost:3000
```

**✅ CHECKPOINT:** Register a user, see dashboard

---

### **HOUR 6-7: Chrome Extension** ✅ DONE
```bash
# 1. Create extension folder
mkdir chrome-extension
cd chrome-extension

# 2. Create popup.html (from artifact 4)

# 3. Create manifest.json:
{
  "manifest_version": 3,
  "name": "Green Coin Tracker",
  "version": "1.0",
  "description": "Earn rewards for closing tabs",
  "permissions": ["tabs", "storage"],
  "action": {
    "default_popup": "popup.html"
  }
}

# 4. Load extension:
# - Open chrome://extensions/
# - Enable "Developer mode"
# - Click "Load unpacked"
# - Select chrome-extension folder

# 5. Click extension icon, enter User ID from frontend
```

**✅ CHECKPOINT:** Extension shows your balance

---

### **HOUR 8-9: Demo Data & Polish**

**Seed demo data:**
```bash
curl -X POST http://localhost:8000/seed-demo-data
```

**Add these features:**

1. **Auto-refresh** in frontend (add to App.js):
```javascript
useEffect(() => {
  const interval = setInterval(() => {
    if (userId) {
      fetchBalance();
      fetchLeaderboard();
    }
  }, 10000); // Every 10 seconds
  return () => clearInterval(interval);
}, [userId]);
```

2. **Better UI animations** (add to Tailwind classes):
```javascript
className="... transform hover:scale-105 transition-all duration-200"
```

3. **Sound effects** (optional, if time):
```javascript
const playSound = () => {
  const audio = new Audio('https://www.soundjay.com/button/sounds/button-09a.mp3');
  audio.play();
};
```

---

### **HOUR 10: Presentation Slides**

**Create a simple deck (Google Slides or PowerPoint):**

**Slide 1: Problem**
- "Bitcoin mining wastes energy"
- "Companies don't optimize servers"
- "Individuals have no eco-incentives"

**Slide 2: Solution**
- "Green Coin: Proof-of-Savings, not Proof-of-Work"
- "Earn coins for sustainable actions"
- "Redeem for cash or rewards"

**Slide 3: Demo**
- Show live dashboard
- Close tabs in extension
- Watch coins increase
- Show leaderboard

**Slide 4: How It Works**
- User performs eco-action
- System calculates CO₂ saved
- Mints coins (1:49 split)
- User redeems rewards

**Slide 5: Market**
- Individual users (gamification)
- AI companies (OpenAI, Anthropic)
- Enterprise servers (cost savings)

**Slide 6: Tech Stack**
- FastAPI backend
- React frontend
- In-memory DB (scales to PostgreSQL)
- Ready for Algorand blockchain

**Slide 7: Business Model**
- 5% transaction fee on redemptions
- Partnerships with retailers
- Enterprise tier for companies

**Slide 8: Roadmap**
- Q1: Browser extension + mobile app
- Q2: Life360 driving integration
- Q3: Enterprise server monitoring
- Q4: Algorand mainnet launch

---

### **HOUR 11: Practice Demo**

**Demo script (3 minutes):**

1. **Intro (30s):**
   "Hi, we're Green Coin. Bitcoin mining wastes energy—we flip that. Instead of burning electricity, you EARN coins by SAVING energy."

2. **Problem (30s):**
   "3 problems: Bitcoin's carbon footprint is huge. Companies don't optimize servers. Individuals have no eco-incentive. We solve all three."

3. **Demo (90s):**
   - Open dashboard: "This is Alice. She's earned 5 coins from eco-actions."
   - Click "Close Tabs": "She just closed 10 tabs, saved 0.05kg CO₂, earned 0.075 coins."
   - Show extension: "Our Chrome extension tracks in real-time."
   - Show leaderboard: "Gamification keeps users engaged."

4. **How it works (30s):**
   "Every action → CO₂ calculation → Coin minting → Dual wallets (cash + points) → Redeem for rewards. We take 5% fee, you get value instantly."

5. **Market (30s):**
   "3 markets: Individuals (loyalty program), AI companies (optimize ChatGPT usage), Enterprises (server cost savings). $10B TAM."

6. **Close (30s):**
   "We're ready to launch. Backend works. Frontend works. Extension works. Next: mobile app, partnerships, Algorand mainnet. Thank you!"

**Practice this 5 times until it's smooth.**

---

### **HOUR 12: Buffer & Final Testing**

**Final checks:**
- [ ] Backend responds to all API calls
- [ ] Frontend shows real-time data
- [ ] Extension tracks tabs correctly
- [ ] Demo data seeded
- [ ] Slides ready
- [ ] Demo script memorized

**If you have extra time:**
- Add more action types (recycling, public transport)
- Improve UI colors/animations
- Add a "Redeem" flow (even if fake)
- Deploy to Vercel/Heroku (optional)

---

## 🎯 WHAT JUDGES WANT TO SEE

1. **Working demo** (most important)
2. **Clear problem/solution**
3. **Real CO₂ calculations** (not fake numbers)
4. **Business model** (how you make money)
5. **Scalability** (mention Algorand)
6. **Passion** (you believe in this)

---

## 🚀 QUICK SETUP COMMANDS

**Terminal 1 (Backend):**
```bash
cd green-coin-hackathon
uvicorn hackathon_backend:app --reload
```

**Terminal 2 (Frontend):**
```bash
cd green-coin-frontend
npm start
```

**Browser:**
- Frontend: http://localhost:3000
- API Docs: http://localhost:8000/docs
- Extension: chrome://extensions/ → Load unpacked

---

## 🎤 PITCH TEMPLATE

> "Imagine if every time you closed a browser tab, you earned money. Not by mining Bitcoin and burning energy—but by SAVING energy. That's Green Coin.
>
> We're a Proof-of-Savings economy. Close tabs, drive efficiently, optimize AI usage—earn coins. Redeem for cash or rewards. We take a 5% fee, like Stripe.
>
> Our target: 1 billion internet users, 10,000 AI companies, 100,000 enterprises. We're carbon-negative by design, powered by Algorand.
>
> We built a working demo in 12 hours. Imagine what we'll build in 12 months. Join the green revolution."

---

## 📦 FILES YOU NEED

```
green-coin-hackathon/
├── hackathon_backend.py  (Artifact 2)
├── requirements.txt      (fastapi, uvicorn, pydantic)

green-coin-frontend/
├── src/
│   └── App.js            (Artifact 3)
├── package.json
└── README.md

chrome-extension/
├── popup.html            (Artifact 4)
├── manifest.json
└── icon.png              (any green leaf icon)
```

---

## 🏆 WINNING STRATEGY

**What makes you stand out:**
1. **Actually works** (not just slides)
2. **Real impact** (CO₂ calculations, not vibes)
3. **Clear monetization** (5% fee)
4. **Scalable** (Algorand blockchain ready)
5. **Timely** (AI energy is a hot topic)

**Bonus points:**
- Mention Algorand's carbon-negative certification
- Show you understand crypto WITHOUT being a crypto bro
- Emphasize BOTH individual AND enterprise markets
- Explain why Proof-of-Savings > Proof-of-Work

---

## ⚡ EMERGENCY SHORTCUTS

**If backend breaks:**
- Use mock data in frontend
- Fake API calls with setTimeout

**If frontend breaks:**
- Use API docs at /docs
- Show Postman screenshots

**If extension breaks:**
- Just demo frontend
- Explain extension concept

**If everything breaks:**
- Show slides + explain
- Talk about vision
- Judges care about idea + execution, not perfection

---

## 🎯 GO TIME CHECKLIST

- [ ] Backend running at :8000
- [ ] Frontend running at :3000
- [ ] Extension loaded in Chrome
- [ ] Demo data seeded
- [ ] 3-minute pitch ready
- [ ] Laptop charged
- [ ] Screen mirroring tested
- [ ] Backup slides on phone

---

**YOU'VE GOT THIS! 🚀**

Focus on the demo, tell a story, show passion. Hackathon judges LOVE seeing something that actually works. You have a working MVP—that's 90% of the battle.

Good luck! 🌿💰