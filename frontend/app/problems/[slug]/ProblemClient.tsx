"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { submitCode, ProblemDetail } from "@/lib/api";
import VerdictPanel from "@/components/VerdictPanel";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

const STARTERS: Record<string, string> = {
  JAVASCRIPT: `// Read from stdin\nconst lines = require("fs").readFileSync("/dev/stdin","utf-8").trim().split("\\n");\n\n// Write your solution here\n`,
  JAVA: `import java.util.Scanner;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your solution here\n    }\n}\n`,
};

const LANG_MONACO: Record<string, string> = {
  JAVASCRIPT: "javascript",
  JAVA: "java",
};

interface Props {
  problem: ProblemDetail;
}

export default function ProblemClient({ problem }: Props) {
  const [language, setLanguage] = useState("JAVASCRIPT");
  const [code, setCode] = useState(STARTERS["JAVASCRIPT"]);
  const [submitting, setSubmitting] = useState(false);
  const [submissionId, setSubmissionId] = useState<number | null>(null);
  const [error, setError] = useState("");

  function handleLanguageChange(lang: string) {
    setLanguage(lang);
    setCode(STARTERS[lang] ?? "");
    setSubmissionId(null);
    setError("");
  }

  async function handleSubmit() {
    setError("");
    setSubmissionId(null);
    setSubmitting(true);
    try {
      const res = await submitCode(problem.slug, language, code);
      setSubmissionId(res.id);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Submission failed. Are you logged in?");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex h-[calc(100vh-52px)]">
      {/* Left — problem statement */}
      <div className="w-[42%] border-r border-gray-700 overflow-y-auto p-6">
        <h1 className="text-xl font-bold text-white mb-1">{problem.title}</h1>
        <div className="flex gap-3 text-xs text-gray-500 mb-4">
          <span className={
            problem.difficulty === "EASY" ? "text-green-400" :
            problem.difficulty === "MEDIUM" ? "text-yellow-400" : "text-red-400"
          }>
            {problem.difficulty.charAt(0) + problem.difficulty.slice(1).toLowerCase()}
          </span>
          <span>Time: {problem.timeLimitMs / 1000}s</span>
          <span>Memory: {problem.memoryLimitMb}MB</span>
        </div>

        <div className="prose text-sm">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {problem.statement}
          </ReactMarkdown>
        </div>

        {problem.sampleTests.length > 0 && (
          <div className="mt-4">
            <h2 className="text-sm font-semibold text-gray-300 mb-2">Sample Tests</h2>
            {problem.sampleTests.map(t => (
              <div key={t.index} className="mb-3">
                <p className="text-xs text-gray-500 mb-1">Example {t.index}</p>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Input</p>
                    <pre className="bg-gray-800 border border-gray-700 rounded p-2 text-xs text-gray-200 whitespace-pre-wrap">{t.input}</pre>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Output</p>
                    <pre className="bg-gray-800 border border-gray-700 rounded p-2 text-xs text-gray-200 whitespace-pre-wrap">{t.expectedOutput}</pre>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Right — editor + submit */}
      <div className="flex-1 flex flex-col">
        <div className="flex items-center justify-between px-4 py-2 border-b border-gray-700 bg-gray-900">
          <select
            value={language}
            onChange={e => handleLanguageChange(e.target.value)}
            className="bg-gray-800 border border-gray-600 text-gray-200 text-sm rounded px-2 py-1 focus:outline-none"
          >
            <option value="JAVASCRIPT">JavaScript</option>
            <option value="JAVA">Java</option>
          </select>

          <button
            onClick={handleSubmit}
            disabled={submitting}
            className="bg-green-500 hover:bg-green-400 disabled:opacity-50 text-black font-semibold text-sm px-4 py-1.5 rounded transition-colors"
          >
            {submitting ? "Submitting..." : "Submit"}
          </button>
        </div>

        <div className="flex-1">
          <MonacoEditor
            height="100%"
            language={LANG_MONACO[language]}
            theme="vs-dark"
            value={code}
            onChange={v => setCode(v ?? "")}
            options={{
              fontSize: 13,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              tabSize: 2,
            }}
          />
        </div>

        {error && (
          <div className="px-4 py-2 bg-red-900/20 border-t border-red-800 text-red-400 text-sm">
            {error}
          </div>
        )}

        {submissionId && (
          <div className="px-4 pb-4">
            <VerdictPanel submissionId={submissionId} />
          </div>
        )}
      </div>
    </div>
  );
}
