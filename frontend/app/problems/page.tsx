import Link from "next/link";
import { getProblems, ProblemSummary } from "@/lib/api";

const difficultyColor: Record<string, string> = {
  EASY: "text-green-400",
  MEDIUM: "text-yellow-400",
  HARD: "text-red-400",
};

export default async function ProblemsPage() {
  let problems: ProblemSummary[] = [];
  try {
    problems = await getProblems();
  } catch {
    // show empty state
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-white mb-6">Problems</h1>
      <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-gray-700 text-gray-400 text-sm">
              <th className="text-left px-4 py-3 font-medium">#</th>
              <th className="text-left px-4 py-3 font-medium">Title</th>
              <th className="text-left px-4 py-3 font-medium">Difficulty</th>
              <th className="text-left px-4 py-3 font-medium">Solved</th>
            </tr>
          </thead>
          <tbody>
            {problems.length === 0 && (
              <tr>
                <td colSpan={4} className="text-center text-gray-500 py-8">
                  No problems yet.
                </td>
              </tr>
            )}
            {problems.map((p, i) => (
              <tr
                key={p.id}
                className="border-b border-gray-800 hover:bg-gray-800 transition-colors"
              >
                <td className="px-4 py-3 text-gray-500 text-sm">{i + 1}</td>
                <td className="px-4 py-3">
                  <Link
                    href={`/problems/${p.slug}`}
                    className="text-blue-400 hover:text-blue-300 font-medium"
                  >
                    {p.title}
                  </Link>
                </td>
                <td className={`px-4 py-3 text-sm font-medium ${difficultyColor[p.difficulty] ?? "text-gray-400"}`}>
                  {p.difficulty.charAt(0) + p.difficulty.slice(1).toLowerCase()}
                </td>
                <td className="px-4 py-3 text-gray-400 text-sm">{p.solvedCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
