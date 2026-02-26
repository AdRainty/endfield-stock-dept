#!/bin/bash

# 终末地调度券交易系统 - API 测试脚本

BASE_URL="http://localhost:8081/api"
TOKEN=""

echo "=========================================="
echo "终末地调度券交易系统 - API 接口测试报告"
echo "=========================================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASS=0
FAIL=0
SKIP=0

test_api() {
    local name=$1
    local method=$2
    local url=$3
    local expected_role=$4

    echo -ne "${YELLOW}测试：${name}... ${NC}"

    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "${BASE_URL}${url}" -H "Authorization: $TOKEN")
    elif [ "$method" == "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}${url}" -H "Content-Type: application/json" -H "Authorization: ${TOKEN}" -d "$5")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    code=$(echo "$body" | jq -r '.code // 500')
    msg=$(echo "$body" | jq -r '.message // ""')

    if [ "$http_code" == "200" ] && [ "$code" == "0" ]; then
        echo -e "${GREEN}✓ 通过${NC}"
        ((PASS++))
    elif [[ "$msg" == *"无此角色"* ]] || [ "$code" == "403" ]; then
        echo -e "${BLUE}⊘ 跳过 (需要${expected_role}角色)${NC}"
        ((SKIP++))
    else
        echo -e "${RED}✗ 失败 (code=$code)${NC}"
        ((FAIL++))
    fi

    if echo "$body" | jq -e '.data.token' >/dev/null 2>&1; then
        TOKEN=$(echo "$body" | jq -r '.data.token')
    fi
}

echo -e "${BLUE}[准备] 获取登录 Token...${NC}"
TOKEN=$(curl -s -X POST "${BASE_URL}/auth/wx-login" -H "Content-Type: application/json" -d '{"code":"mock_api_test","nickname":"API 测试用户"}' | jq -r '.data.token')
echo "Token: ${TOKEN:0:36}..."
echo ""

echo -e "${BLUE}[1. 交易所管理]${NC}"
test_api "获取交易所列表" "GET" "/exchange/list"
test_api "获取交易所品种" "GET" "/exchange/1/instruments"

echo ""
echo -e "${BLUE}[2. 行情管理]${NC}"
test_api "获取所有品种行情" "GET" "/market/instruments"
test_api "获取品种详情" "GET" "/market/instrument/VL_ENERGY"
test_api "获取档口数据" "GET" "/market/orderbook/1/VL_ENERGY"

echo ""
echo -e "${BLUE}[3. 用户认证]${NC}"
test_api "获取当前用户信息" "GET" "/auth/user-info"

echo ""
echo -e "${BLUE}[4. 交易管理]${NC}"
test_api "获取资金账户" "GET" "/trade/account/1"
test_api "获取持仓列表" "GET" "/trade/position/1"
test_api "获取订单列表" "GET" "/trade/orders"

echo ""
echo -e "${BLUE}[5. 管理员接口]${NC}"
test_api "获取用户列表" "GET" "/admin/users" "ADMIN"
test_api "获取统计数据" "GET" "/admin/statistics" "ADMIN"

echo ""
echo "=========================================="
echo -e "结果：${GREEN}通过 ${PASS}${NC} / ${BLUE}跳过 ${SKIP}${NC} / ${RED}失败 ${FAIL}${NC}"
echo "=========================================="
