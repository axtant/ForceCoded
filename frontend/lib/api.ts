const BASE =
  typeof window === "undefined"
    ? process.env.BACKEND_URL || "http://34.180.54.4:8080"
    : process.env.NEXT_PUBLIC_API_URL || "";

function authHeaders(): HeadersInit {
  const token = typeof window !== "undefined" ? localStorage.getItem("token") : null;
  return token
    ? { "Content-Type": "application/json", Authorization: `Bearer ${token}` }
    : { "Content-Type": "application/json" };
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: authHeaders(),
    ...init,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
}

// Auth
export const register = (username: string, email: string, password: string) =>
  request<{ token: string }>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, email, password }),
  });

export const login = (username: string, password: string) =>
  request<{ token: string }>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

// Problems
export interface ProblemSummary {
  id: number;
  slug: string;
  title: string;
  difficulty: string;
  solvedCount: number;
}

export interface SampleTest {
  index: number;
  input: string;
  expectedOutput: string;
}

export interface ProblemDetail extends ProblemSummary {
  statement: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  sampleTests: SampleTest[];
}

export const getProblems = () => request<ProblemSummary[]>("/api/problems");
export const getProblem = (slug: string) => request<ProblemDetail>(`/api/problems/${slug}`);

// Submissions
export interface SubmissionResponse {
  id: number;
  problemSlug: string;
  language: string;
  verdict: string;
  createdAt: string;
}

export interface TestResult {
  testIndex: number;
  verdict: string;
  executionTimeMs: number;
}

export interface SubmissionDetail extends SubmissionResponse {
  executionTimeMs: number | null;
  results: TestResult[];
}

export const submitCode = (
  problemSlug: string,
  language: string,
  code: string,
  contestId?: number
) =>
  request<SubmissionResponse>("/api/submissions", {
    method: "POST",
    body: JSON.stringify({ problemSlug, language, code, contestId }),
  });

export const getSubmission = (id: number) =>
  request<SubmissionDetail>(`/api/submissions/${id}`);

// Contests
export interface ContestSummary {
  id: number;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: string;
  problemCount: number;
}

export interface ContestProblem {
  label: string;
  orderIndex: number;
  problemId: number;
  slug: string;
  title: string;
  difficulty: string;
}

export interface ContestDetail extends ContestSummary {
  problems: ContestProblem[];
}

export interface ProblemStanding {
  label: string;
  solved: boolean;
  attempts: number;
  penaltyMinutes: number | null;
}

export interface StandingsRow {
  rank: number;
  username: string;
  solved: number;
  penalty: number;
  problems: ProblemStanding[];
}

export const getContests = () => request<ContestSummary[]>("/api/contests");
export const getContest = (id: number) => request<ContestDetail>(`/api/contests/${id}`);
export const getStandings = (id: number) => request<StandingsRow[]>(`/api/contests/${id}/standings`);
