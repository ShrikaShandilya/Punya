import React, { useState, useEffect } from 'react';
import { Leaf, TrendingUp, Award, Zap, DollarSign, Gift } from 'lucide-react';

const API_URL = 'http://localhost:8000';

export default function GreenCoinDashboard() {
  const [userId, setUserId] = useState(localStorage.getItem('userId') || '');
  const [username, setUsername] = useState('');
  const [balance, setBalance] = useState(null);
  const [stats, setStats] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(false);
  const [view, setView] = useState('dashboard');

  useEffect(() => {
    if (userId) {
      fetchBalance();
      fetchStats();
    }
    fetchLeaderboard();
  }, [userId]);

  const register = async () => {
    if (!username) return;
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email: `${username}@demo.com` })
      });
      const data = await res.json();
      setUserId(data.user_id);
      localStorage.setItem('userId', data.user_id);
      await fetchBalance();
    } catch (err) {
      console.error(err);
    }
    setLoading(false);
  };

  const fetchBalance = async () => {
    try {
      const res = await fetch(`${API_URL}/balance/${userId}`);
      const data = await res.json();
      setBalance(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchStats = async () => {
    try {
      const res = await fetch(`${API_URL}/stats`);
      const data = await res.json();
      setStats(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchLeaderboard = async () => {
    try {
      const res = await fetch(`${API_URL}/leaderboard`);
      const data = await res.json();
      setLeaderboard(data.leaderboard);
    } catch (err) {
      console.error(err);
    }
  };

  const logAction = async (actionType, metadata) => {
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/action`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ user_id: userId, action_type: actionType, metadata })
      });
      const data = await res.json();
      alert(`✅ ${data.message}\n💰 +${data.coins_minted.toFixed(2)} coins\n🔥 ${data.streak_days} day streak!`);
      await fetchBalance();
      await fetchStats();
      await fetchLeaderboard();
    } catch (err) {
      console.error(err);
    }
    setLoading(false);
  };

  if (!userId) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full">
          <div className="text-center mb-6">
            <Leaf className="w-16 h-16 text-green-600 mx-auto mb-4" />
            <h1 className="text-3xl font-bold text-gray-800 mb-2">Green Coin</h1>
            <p className="text-gray-600">Earn rewards for sustainable actions</p>
          </div>
          <input
            type="text"
            placeholder="Enter your username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg mb-4 focus:border-green-500 focus:outline-none"
          />
          <button
            onClick={register}
            disabled={loading || !username}
            className="w-full bg-green-600 text-white py-3 rounded-lg font-semibold hover:bg-green-700 disabled:bg-gray-400 transition"
          >
            {loading ? 'Creating...' : 'Get Started'}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 p-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Leaf className="w-10 h-10 text-green-600" />
              <div>
                <h1 className="text-2xl font-bold text-gray-800">Green Coin</h1>
                <p className="text-gray-600">Welcome, {balance?.username}</p>
              </div>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setView('dashboard')}
                className={`px-4 py-2 rounded-lg font-medium transition ${view === 'dashboard' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
              >
                Dashboard
              </button>
              <button
                onClick={() => setView('actions')}
                className={`px-4 py-2 rounded-lg font-medium transition ${view === 'actions' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
              >
                Actions
              </button>
              <button
                onClick={() => setView('leaderboard')}
                className={`px-4 py-2 rounded-lg font-medium transition ${view === 'leaderboard' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
              >
                Leaderboard
              </button>
            </div>
          </div>
        </div>

        {view === 'dashboard' && balance && (
          <>
            {/* Wallets */}
            <div className="grid md:grid-cols-2 gap-6 mb-6">
              <div className="bg-gradient-to-br from-green-600 to-green-700 rounded-2xl shadow-lg p-6 text-white">
                <div className="flex items-center justify-between mb-4">
                  <span className="text-green-100">Money Wallet</span>
                  <DollarSign className="w-6 h-6" />
                </div>
                <div className="text-4xl font-bold mb-2">{balance.wallets.money.balance_coins.toFixed(2)}</div>
                <div className="text-green-100">${balance.wallets.money.balance_usd.toFixed(2)} USD</div>
              </div>

              <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl shadow-lg p-6 text-white">
                <div className="flex items-center justify-between mb-4">
                  <span className="text-blue-100">Points Wallet</span>
                  <Gift className="w-6 h-6" />
                </div>
                <div className="text-4xl font-bold mb-2">{balance.wallets.points.balance_coins.toFixed(2)}</div>
                <div className="text-blue-100">${balance.wallets.points.balance_usd.toFixed(2)} value</div>
              </div>
            </div>

            {/* Stats */}
            <div className="grid md:grid-cols-3 gap-6 mb-6">
              <div className="bg-white rounded-xl shadow-lg p-6">
                <div className="flex items-center gap-3 mb-2">
                  <Leaf className="w-8 h-8 text-green-600" />
                  <span className="text-gray-600 font-medium">CO₂ Saved</span>
                </div>
                <div className="text-3xl font-bold text-gray-800">{balance.stats.total_co2_saved_kg.toFixed(1)} kg</div>
              </div>

              <div className="bg-white rounded-xl shadow-lg p-6">
                <div className="flex items-center gap-3 mb-2">
                  <Zap className="w-8 h-8 text-orange-600" />
                  <span className="text-gray-600 font-medium">Streak</span>
                </div>
                <div className="text-3xl font-bold text-gray-800">{balance.stats.current_streak_days} days</div>
              </div>

              <div className="bg-white rounded-xl shadow-lg p-6">
                <div className="flex items-center gap-3 mb-2">
                  <TrendingUp className="w-8 h-8 text-blue-600" />
                  <span className="text-gray-600 font-medium">Actions</span>
                </div>
                <div className="text-3xl font-bold text-gray-800">{balance.stats.actions_count}</div>
              </div>
            </div>

            {/* Global Stats */}
            {stats && (
              <div className="bg-white rounded-2xl shadow-lg p-6">
                <h2 className="text-xl font-bold text-gray-800 mb-4">Global Impact</h2>
                <div className="grid md:grid-cols-4 gap-4">
                  <div>
                    <div className="text-gray-600 text-sm">Total Users</div>
                    <div className="text-2xl font-bold text-green-600">{stats.total_users}</div>
                  </div>
                  <div>
                    <div className="text-gray-600 text-sm">CO₂ Saved</div>
                    <div className="text-2xl font-bold text-green-600">{stats.total_co2_saved_kg.toFixed(1)} kg</div>
                  </div>
                  <div>
                    <div className="text-gray-600 text-sm">Trees Equivalent</div>
                    <div className="text-2xl font-bold text-green-600">{stats.trees_equivalent} 🌲</div>
                  </div>
                  <div>
                    <div className="text-gray-600 text-sm">Total Actions</div>
                    <div className="text-2xl font-bold text-green-600">{stats.total_actions}</div>
                  </div>
                </div>
              </div>
            )}
          </>
        )}

        {view === 'actions' && (
          <div className="grid md:grid-cols-2 gap-6">
            <button
              onClick={() => logAction('close_tabs', { tabs: Math.floor(Math.random() * 15) + 5 })}
              disabled={loading}
              className="bg-white rounded-2xl shadow-lg p-8 hover:shadow-xl transition text-left disabled:opacity-50"
            >
              <div className="text-4xl mb-3">🗂️</div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">Close Browser Tabs</h3>
              <p className="text-gray-600">Reduce energy by closing unused tabs</p>
            </button>

            <button
              onClick={() => logAction('efficient_drive', { distance_km: Math.floor(Math.random() * 20) + 5, efficiency: 'good' })}
              disabled={loading}
              className="bg-white rounded-2xl shadow-lg p-8 hover:shadow-xl transition text-left disabled:opacity-50"
            >
              <div className="text-4xl mb-3">🚗</div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">Efficient Driving</h3>
              <p className="text-gray-600">Log your eco-friendly commute</p>
            </button>

            <button
              onClick={() => logAction('ai_optimize', { prompts_optimized: Math.floor(Math.random() * 10) + 5 })}
              disabled={loading}
              className="bg-white rounded-2xl shadow-lg p-8 hover:shadow-xl transition text-left disabled:opacity-50"
            >
              <div className="text-4xl mb-3">🤖</div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">AI Optimization</h3>
              <p className="text-gray-600">Use efficient AI prompts</p>
            </button>

            <button
              onClick={() => logAction('server_optimize', { kwh_saved: Math.floor(Math.random() * 5) + 1 })}
              disabled={loading}
              className="bg-white rounded-2xl shadow-lg p-8 hover:shadow-xl transition text-left disabled:opacity-50"
            >
              <div className="text-4xl mb-3">💻</div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">Server Optimization</h3>
              <p className="text-gray-600">Optimize server resource usage</p>
            </button>
          </div>
        )}

        {view === 'leaderboard' && (
          <div className="bg-white rounded-2xl shadow-lg p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
              <Award className="w-8 h-8 text-yellow-500" />
              Leaderboard
            </h2>
            <div className="space-y-3">
              {leaderboard.map((user, idx) => (
                <div
                  key={idx}
                  className={`flex items-center justify-between p-4 rounded-lg ${
                    idx < 3 ? 'bg-gradient-to-r from-yellow-50 to-yellow-100' : 'bg-gray-50'
                  }`}
                >
                  <div className="flex items-center gap-4">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold ${
                      idx === 0 ? 'bg-yellow-400 text-white' :
                      idx === 1 ? 'bg-gray-400 text-white' :
                      idx === 2 ? 'bg-orange-400 text-white' :
                      'bg-gray-200 text-gray-700'
                    }`}>
                      {user.rank}
                    </div>
                    <div>
                      <div className="font-bold text-gray-800">{user.username}</div>
                      <div className="text-sm text-gray-600">{user.co2_saved_kg} kg CO₂ saved</div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-green-600">{user.total_coins.toFixed(1)} coins</div>
                    <div className="text-sm text-gray-600">🔥 {user.streak_days} days</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}