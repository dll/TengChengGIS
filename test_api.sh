#!/bin/bash
echo "滁州亭城GIS系统API测试脚本"

echo "1. 测试获取所有亭子位置..."
curl -s http://localhost:8092/thousand-pavilions/locations | python -m json.tool

echo -e "\n2. 测试获取两亭之间的路线..."
curl -s http://localhost:8092/thousand-pavilions/route/1/2 | python -m json.tool

echo -e "\n3. 测试获取亭子多媒体信息..."
curl -s http://localhost:8092/thousand-pavilions/multimedia/1 | python -m json.tool

echo -e "\n4. 测试主页..."
curl -s -I http://localhost:8092 | head -n 10

echo -e "\n测试完成！"