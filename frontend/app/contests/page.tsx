import Link from "next/link";
import { getContests, ContestSummary } from "@/lib/api";

export const dynamic = "force-dynamic";

const statusColor: Record<string, string> = {
  UPCOMING: "text-blue-400 bg-blue-900/30 border-blue-700",
  ACTIVE: "text-green-400 bg-green-900/30 border-green-700",
  FINISHED: "text-gray-400 bg-gray-800 border-gray-600",
};

function formatDate(iso: string) {
  return new Date(iso).toLocaleString(undefined, {
    month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}

export default async function ContestsPage() {
  let contests: ContestSummary[] = [];
  try {
    contests = await getContests();
  } catch {
    // show empty
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-white mb-6">Contests</h1>

      {contests.length === 0 && (
        <p className="text-gray-500">No contests yet.</p>
      )}

      <div className="flex flex-col gap-3">
        {contests.map(c => (
          <Link
            key={c.id}
            href={`/contests/${c.id}`}
            className="bg-gray-900 border border-gray-700 hover:border-gray-500 rounded-lg p-4 flex items-center justify-between transition-colors"
          >
            <div>
              <div className="flex items-center gap-3 mb-1">
                <span className="text-white font-medium">{c.title}</span>
                <span className={`text-xs border px-2 py-0.5 rounded ${statusColor[c.status] ?? "text-gray-400"}`}>
                  {c.status}
                </span>
              </div>
              <p className="text-gray-500 text-sm">
                {formatDate(c.startTime)} — {formatDate(c.endTime)}
              </p>
            </div>
            <div className="text-gray-500 text-sm text-right">
              <p>{c.problemCount} problems</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
