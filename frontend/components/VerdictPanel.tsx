"use client";

import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getSubmission } from "@/lib/api";

interface TestUpdate {
  testIndex: number;
  status: string;
}

interface FinalVerdict {
  verdict: string;
}

interface Props {
  submissionId: number;
  onDone?: (verdict: string) => void;
}

const FINAL_VERDICTS = new Set([
  "ACCEPTED", "WRONG_ANSWER", "TIME_LIMIT_EXCEEDED",
  "MEMORY_LIMIT_EXCEEDED", "RUNTIME_ERROR", "COMPILATION_ERROR",
]);

const verdictColor: Record<string, string> = {
  ACCEPTED: "text-green-400",
  WRONG_ANSWER: "text-red-400",
  TIME_LIMIT_EXCEEDED: "text-yellow-400",
  MEMORY_LIMIT_EXCEEDED: "text-yellow-400",
  RUNTIME_ERROR: "text-orange-400",
  COMPILATION_ERROR: "text-orange-400",
  IN_PROGRESS: "text-blue-400",
  QUEUED: "text-gray-400",
};

const verdictLabel: Record<string, string> = {
  ACCEPTED: "Accepted",
  WRONG_ANSWER: "Wrong Answer",
  TIME_LIMIT_EXCEEDED: "Time Limit Exceeded",
  MEMORY_LIMIT_EXCEEDED: "Memory Limit Exceeded",
  RUNTIME_ERROR: "Runtime Error",
  COMPILATION_ERROR: "Compilation Error",
  IN_PROGRESS: "Judging...",
  QUEUED: "Queued",
};

export default function VerdictPanel({ submissionId, onDone }: Props) {
  const [tests, setTests] = useState<Record<number, string>>({});
  const [finalVerdict, setFinalVerdict] = useState<string | null>(null);
  const doneRef = useRef(false);

  function settle(verdict: string, testResults?: Record<number, string>) {
    if (doneRef.current) return;
    doneRef.current = true;
    if (testResults) setTests(testResults);
    setFinalVerdict(verdict);
    onDone?.(verdict);
  }

  useEffect(() => {
    doneRef.current = false;

    // --- WebSocket (primary) ---
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL!;
    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
      reconnectDelay: 0,
      onConnect: () => {
        client.subscribe(`/topic/submission/${submissionId}`, (msg) => {
          const data = JSON.parse(msg.body);
          if ("testIndex" in data) {
            const update = data as TestUpdate;
            setTests(prev => ({ ...prev, [update.testIndex]: update.status }));
          } else if ("verdict" in data) {
            const v = (data as FinalVerdict).verdict;
            if (FINAL_VERDICTS.has(v)) {
              settle(v);
              client.deactivate();
            }
          }
        });
      },
    });
    client.activate();

    // --- Polling fallback (kicks in if WebSocket misses the verdict) ---
    const pollInterval = setInterval(async () => {
      if (doneRef.current) { clearInterval(pollInterval); return; }
      try {
        const sub = await getSubmission(submissionId);
        if (FINAL_VERDICTS.has(sub.verdict)) {
          const testMap: Record<number, string> = {};
          sub.results.forEach(r => {
            testMap[r.testIndex] = r.verdict === "ACCEPTED" ? "AC"
              : r.verdict === "WRONG_ANSWER" ? "WA"
              : r.verdict === "TIME_LIMIT_EXCEEDED" ? "TLE"
              : r.verdict === "RUNTIME_ERROR" ? "RE" : r.verdict;
          });
          settle(sub.verdict, testMap);
          clearInterval(pollInterval);
          client.deactivate();
        }
      } catch {
        // ignore poll errors, keep trying
      }
    }, 3000);

    return () => {
      client.deactivate();
      clearInterval(pollInterval);
    };
  }, [submissionId]);

  const testEntries = Object.entries(tests).sort(([a], [b]) => Number(a) - Number(b));

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-4 mt-4">
      <h3 className="text-gray-400 text-sm font-medium mb-3">Judge Results</h3>

      {testEntries.length === 0 && !finalVerdict && (
        <p className="text-gray-500 text-sm animate-pulse">Waiting for judge...</p>
      )}

      <div className="flex flex-wrap gap-2 mb-3">
        {testEntries.map(([idx, status]) => (
          <div
            key={idx}
            className={`text-xs px-2 py-1 rounded border ${
              status === "AC"
                ? "border-green-700 bg-green-900/30 text-green-400"
                : status === "WA"
                ? "border-red-700 bg-red-900/30 text-red-400"
                : status === "TLE"
                ? "border-yellow-700 bg-yellow-900/30 text-yellow-400"
                : "border-orange-700 bg-orange-900/30 text-orange-400"
            }`}
          >
            Test {idx}: {status}
          </div>
        ))}
      </div>

      {finalVerdict && (
        <div className={`text-lg font-bold mt-2 ${verdictColor[finalVerdict] ?? "text-gray-300"}`}>
          {verdictLabel[finalVerdict] ?? finalVerdict}
        </div>
      )}
    </div>
  );
}
