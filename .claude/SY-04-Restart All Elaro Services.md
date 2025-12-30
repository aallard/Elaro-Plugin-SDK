# SY-04: Restart All Elaro Services (REFERENCE THIS FOR EVERY RESTART)

## CRITICAL: ALL 4 SERVICES MUST RUN TOGETHER

| Service | Port | Must Be Running For |
|---------|------|---------------------|
| Console Backend | 8080 | Auth, plugins, IAM, everything |
| Console Frontend | 3000 | Console UI |
| Forge Backend | 8081 | Plan generation, AI |
| Forge Frontend | 5174 | Forge UI (via iframe in Console) |

**Forge menus only appear in Console after beacon is received (~30 sec after Forge backend starts)**

---

## RESTART ALL SERVICES

```bash
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           RESTARTING ALL ELARO SERVICES                    ║"
echo "╚════════════════════════════════════════════════════════════╝"

# Kill all existing
echo "Stopping existing services..."
lsof -ti :8080 | xargs kill -9 2>/dev/null || true
lsof -ti :8081 | xargs kill -9 2>/dev/null || true
lsof -ti :3000 | xargs kill -9 2>/dev/null || true
lsof -ti :5174 | xargs kill -9 2>/dev/null || true
sleep 2

# Start Console Backend
echo "Starting Console Backend (8080)..."
cd ~/Documents/GitHub/Elaro/elaro-console
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev -q > /tmp/console-backend.log 2>&1 &

# Start Forge Backend
echo "Starting Forge Backend (8081)..."
cd ~/Documents/GitHub/Elaro-Forge
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev -q > /tmp/forge-backend.log 2>&1 &

# Wait for backends
echo "Waiting for backends to start..."
for i in {1..30}; do
    CONSOLE_UP=$(curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -c "UP" || echo "0")
    FORGE_UP=$(curl -s http://localhost:8081/actuator/health 2>/dev/null | grep -c "UP" || echo "0")
    
    if [ "$CONSOLE_UP" = "1" ] && [ "$FORGE_UP" = "1" ]; then
        echo "✅ Both backends UP"
        break
    fi
    echo "  Waiting... ($i/30) Console=$CONSOLE_UP Forge=$FORGE_UP"
    sleep 2
done

# Start Console Frontend
echo "Starting Console Frontend (3000)..."
cd ~/Documents/GitHub/Elaro-Console/frontend
nohup npm run dev > /tmp/console-frontend.log 2>&1 &

# Start Forge Frontend
echo "Starting Forge Frontend (5174)..."
cd ~/Documents/GitHub/Elaro-Forge/frontend
nohup npm run dev > /tmp/forge-frontend.log 2>&1 &

# Wait for frontends
sleep 5
echo "Waiting for frontends..."
for i in {1..10}; do
    CONSOLE_FE=$(curl -s http://localhost:3000 2>/dev/null | grep -c "html" || echo "0")
    FORGE_FE=$(curl -s http://localhost:5174 2>/dev/null | grep -c "html" || echo "0")
    
    if [ "$CONSOLE_FE" = "1" ] && [ "$FORGE_FE" = "1" ]; then
        echo "✅ Both frontends UP"
        break
    fi
    sleep 2
done

# Wait for Forge beacon
echo "Waiting for Forge beacon registration (30 sec)..."
sleep 30

# Verify everything
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           SERVICE STATUS                                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo "Console Backend (8080): $(curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -o '"status":"[^"]*"' | head -1 || echo 'DOWN')"
echo "Forge Backend (8081):   $(curl -s http://localhost:8081/actuator/health 2>/dev/null | grep -o '"status":"[^"]*"' | head -1 || echo 'DOWN')"
echo "Console Frontend (3000): $(curl -s http://localhost:3000 2>/dev/null | head -c 20 | grep -c html || echo '0') (1=UP)"
echo "Forge Frontend (5174):   $(curl -s http://localhost:5174 2>/dev/null | head -c 20 | grep -c html || echo '0') (1=UP)"

# Check plugin registration
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@elaro.ai","password":"admin123"}' | jq -r '.data.accessToken' 2>/dev/null)

echo ""
echo "Forge Plugin Status:"
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/plugins 2>/dev/null | jq '.data[] | {pluginId, name, status}' || echo "Could not check plugins"

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           ACCESS URLS                                      ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo "Console: http://localhost:3000"
echo "Login:   admin@elaro.ai / admin123"
echo ""
```

---

## QUICK CHECK STATUS

```bash
echo "Console Backend: $(curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -o 'UP' || echo 'DOWN')"
echo "Forge Backend:   $(curl -s http://localhost:8081/actuator/health 2>/dev/null | grep -o 'UP' || echo 'DOWN')"
echo "Console Frontend: $(curl -s http://localhost:3000 2>/dev/null | head -c 1 | wc -c | xargs test 0 -lt && echo 'UP' || echo 'DOWN')"
echo "Forge Frontend:   $(curl -s http://localhost:5174 2>/dev/null | head -c 1 | wc -c | xargs test 0 -lt && echo 'UP' || echo 'DOWN')"
```

---

## IF FORGE MENUS STILL MISSING

Forge beacon takes ~30 seconds after backend starts. If still missing:

```bash
# Force re-check
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@elaro.ai","password":"admin123"}' | jq -r '.data.accessToken')

# Check if plugin registered
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/plugins | jq '.data'

# If PENDING, approve it
PLUGIN_ID=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/plugins | jq -r '.data[0].id')
curl -s -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/plugins/$PLUGIN_ID/approve
```

---

## RULE FOR CLAUDE CODE

**WHENEVER restarting ANY service, ALWAYS restart ALL 4 services using the script above.**

Never restart just one service — they depend on each other:
- Forge needs Console for auth tokens
- Console needs Forge beacon to show menus
- Both frontends need their backends