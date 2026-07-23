#!/bin/bash
set -e
echo "[1/6] Node.js 20..."
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs
echo "[2/6] PostgreSQL..."
sudo apt-get install -y postgresql postgresql-contrib
sudo systemctl start postgresql && sudo systemctl enable postgresql
sudo -u postgres psql -c "CREATE USER originconnect WITH PASSWORD 'OC_SECURE_2024!';" 2>/dev/null || true
sudo -u postgres psql -c "CREATE DATABASE originconnect OWNER originconnect;" 2>/dev/null || true
echo "[3/6] PM2..."
sudo npm install -g pm2
echo "[4/6] Install dependencies..."
npm install
echo "[5/6] Build..."
npm run build
echo "[6/6] Start..."
pm2 stop originconnect 2>/dev/null || true
pm2 start dist/main.js --name originconnect --max-memory-restart 512M
pm2 save && pm2 startup
echo "Done! API: http://$(curl -s ifconfig.me 2>/dev/null):3000/api/v1"
