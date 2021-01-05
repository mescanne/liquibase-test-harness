#!/bin/sh

HOST="spanner"

STATUS=""
for i in $(seq 1 1 10); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://${HOST}:9020/v1/projects/projectid/instances)
  if [ "${STATUS}" == "200" ]; then
    break
  fi
  echo "Spanner not alive. Still waiting."
  sleep 1
done

if [ "${STATUS}" != "200" ]; then
  echo "Spanner never started."
  exit 1
fi

echo "Spanner instance alive. Initializing."

# Create instance
echo "Creating instance"
curl -H "Content-Type: application/json" --data @create-instance.json -v http://${HOST}:9020/v1/projects/projectid/instances

# Create database
echo "Creating database"
curl -H "Content-Type: application/json" --data @create-database.json -v http://${HOST}:9020/v1/projects/projectid/instances/test-instance/databases

