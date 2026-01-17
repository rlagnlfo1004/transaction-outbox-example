#!/bin/bash

# Transaction Outbox API Test Script

BASE_URL="http://localhost:8080/api/v1/projects"

echo "========================================"
echo "Transaction Outbox Pattern API Test"
echo "========================================"
echo ""

# 1. 프로젝트 신청 생성
echo "1. Creating project request for user 100..."
response=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"userId": 100}')

echo "Response: $response"
echo ""

# Extract project request ID (assuming jq is installed)
if command -v jq &> /dev/null; then
    PROJECT_ID=$(echo $response | jq -r '.projectRequestId')
    echo "Created Project Request ID: $PROJECT_ID"
else
    echo "Note: Install 'jq' for better JSON parsing"
    PROJECT_ID=1
    echo "Using default Project ID: $PROJECT_ID"
fi
echo ""

# 2. 프로젝트 승인
echo "2. Approving project request $PROJECT_ID..."
curl -s -X POST "$BASE_URL/$PROJECT_ID/approve"
echo ""
echo "Project approved!"
echo ""

# 3. 다른 프로젝트 신청 생성
echo "3. Creating another project request for user 200..."
response2=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"userId": 200}')

echo "Response: $response2"
echo ""

if command -v jq &> /dev/null; then
    PROJECT_ID_2=$(echo $response2 | jq -r '.projectRequestId')
    echo "Created Project Request ID: $PROJECT_ID_2"
else
    PROJECT_ID_2=2
    echo "Using default Project ID: $PROJECT_ID_2"
fi
echo ""

# 4. 프로젝트 거절
echo "4. Rejecting project request $PROJECT_ID_2..."
curl -s -X POST "$BASE_URL/$PROJECT_ID_2/reject"
echo ""
echo "Project rejected!"
echo ""

echo "========================================"
echo "Test completed!"
echo "========================================"
echo ""
echo "Check the application logs to see:"
echo "- Outbox messages being saved"
echo "- Scheduler polling for messages"
echo "- Discord messages being sent"
echo ""

