const fs = require('fs');
const { execSync } = require('child_process');

const testDir = '/app/testcases';
const testCount = parseInt(process.env.TEST_COUNT || '0');
const timeLimitMs = parseInt(process.env.TIME_LIMIT_MS || '2000');
const results = [];

for (let t = 1; t <= testCount; t++) {
    const inputPath  = `${testDir}/test_${t}/input.txt`;
    const outputPath = `${testDir}/test_${t}/output.txt`;
    const tmpInput   = `/tmp/judge_input_${t}.txt`;
    const start = Date.now();
    let status = 'AC';
    let actual = '';

    try {
        // write input to a temp file and redirect stdin from it
        // this lets user code read from /dev/stdin or process.stdin
        fs.copyFileSync(inputPath, tmpInput);
        actual = execSync(`node /app/user_solution.js < ${tmpInput}`, {
            timeout: timeLimitMs,
            encoding: 'utf-8',
            shell: true
        }).trim();
    } catch (e) {
        status = (e.killed || e.code === 'ETIMEDOUT') ? 'TLE' : 'RE';
        actual = '';
    }

    const elapsed = Date.now() - start;

    if (status === 'AC') {
        const expected = fs.readFileSync(outputPath, 'utf-8').trim();
        if (actual !== expected) status = 'WA';
    }

    results.push({ test: t, status, time_ms: elapsed });
}

console.log(JSON.stringify(results));
