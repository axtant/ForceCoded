#!/bin/sh

javac /app/Solution.java 2>/tmp/ce.txt
if [ $? -ne 0 ]; then
    echo '{"compile_error":true}'
    exit 0
fi

echo "["
i=1
sep=""
while [ "$i" -le "${TEST_COUNT:-0}" ]; do
    start=$(date +%s)
    timeout "${TIME_LIMIT_SEC:-2}" java -cp /app Solution < "/app/testcases/test_${i}/input.txt" > /tmp/out.txt 2>/dev/null
    rc=$?
    elapsed=$(( ($(date +%s) - start) * 1000 ))

    actual=$(tr -d '\r' < /tmp/out.txt | sed 's/[[:space:]]*$//')
    expected=$(tr -d '\r' < "/app/testcases/test_${i}/output.txt" | sed 's/[[:space:]]*$//')

    if   [ "$rc" -eq 124 ];           then st="TLE"
    elif [ "$rc" -ne 0 ];             then st="RE"
    elif [ "$actual" = "$expected" ]; then st="AC"
    else                                   st="WA"
    fi

    printf '%s{"test":%d,"status":"%s","time_ms":%d}' "$sep" "$i" "$st" "$elapsed"
    sep=","
    i=$((i+1))
done
printf '\n]\n'
