import Link from "next/link";
import { getContest, getStandings } from "@/lib/api";

function formatDate(iso: string) {
  return new Date(iso).toLocaleString(undefined, {
    month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}

const statusColor: Record<string, string> = {
  UPCOMING: "text-blue-400",
  ACTIVE: "text-green-400",
  FINISHED: "text-gray-400",
};

export default async function ContestPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const [contest, standings] = await Promise.all([
    getContest(Number(id)),
    getStandings(Number(id)),
  ]);

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-1">
          <h1 className="text-2xl font-bold text-white">{contest.title}</h1>
          <span className={`text-sm font-medium ${statusColor[contest.status] ?? "text-gray-400"}`}>
            {contest.status}
          </span>
        </div>
        {contest.description && (
          <p className="text-gray-400 text-sm mb-2">{contest.description}</p>
        )}
        <p className="text-gray-500 text-sm">
          {formatDate(contest.startTime)} — {formatDate(contest.endTime)}
        </p>
      </div>

      {/* Problems */}
      <section className="mb-8">
        <h2 className="text-lg font-semibold text-white mb-3">Problems</h2>
        <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700 text-gray-400 text-sm">
                <th className="text-left px-4 py-3 font-medium w-12">#</th>
                <th className="text-left px-4 py-3 font-medium">Title</th>
                <th className="text-left px-4 py-3 font-medium">Difficulty</th>
              </tr>
            </thead>
            <tbody>
              {contest.problems.map(p => (
                <tr key={p.label} className="border-b border-gray-800 hover:bg-gray-800 transition-colors">
                  <td className="px-4 py-3 text-gray-300 font-mono font-bold">{p.label}</td>
                  <td className="px-4 py-3">
                    <Link href={`/problems/${p.slug}`} className="text-blue-400 hover:text-blue-300">
                      {p.title}
                    </Link>
                  </td>
                  <td className={`px-4 py-3 text-sm ${
                    p.difficulty === "EASY" ? "text-green-400" :
                    p.difficulty === "MEDIUM" ? "text-yellow-400" : "text-red-400"
                  }`}>
                    {p.difficulty.charAt(0) + p.difficulty.slice(1).toLowerCase()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Standings */}
      <section>
        <h2 className="text-lg font-semibold text-white mb-3">Standings</h2>
        {standings.length === 0 ? (
          <p className="text-gray-500 text-sm">No submissions yet.</p>
        ) : (
          <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-700 text-gray-400">
                  <th className="text-left px-4 py-3 font-medium w-12">Rank</th>
                  <th className="text-left px-4 py-3 font-medium">User</th>
                  <th className="text-left px-4 py-3 font-medium">Solved</th>
                  <th className="text-left px-4 py-3 font-medium">Penalty</th>
                  {contest.problems.map(p => (
                    <th key={p.label} className="text-center px-3 py-3 font-medium w-16">
                      {p.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {standings.map(row => (
                  <tr key={row.username} className="border-b border-gray-800 hover:bg-gray-800 transition-colors">
                    <td className="px-4 py-3 text-gray-400">{row.rank}</td>
                    <td className="px-4 py-3 text-white font-medium">{row.username}</td>
                    <td className="px-4 py-3 text-green-400 font-bold">{row.solved}</td>
                    <td className="px-4 py-3 text-gray-400">{row.penalty}</td>
                    {row.problems.map(p => (
                      <td key={p.label} className="px-3 py-3 text-center">
                        {p.solved ? (
                          <div>
                            <div className="text-green-400 font-bold text-xs">+{p.attempts > 0 ? p.attempts : ""}</div>
                            <div className="text-gray-500 text-xs">{p.penaltyMinutes}m</div>
                          </div>
                        ) : p.attempts > 0 ? (
                          <div className="text-red-400 text-xs">-{p.attempts}</div>
                        ) : (
                          <div className="text-gray-600 text-xs">—</div>
                        )}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
