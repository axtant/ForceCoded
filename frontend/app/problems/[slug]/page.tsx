import { getProblem } from "@/lib/api";
import ProblemClient from "./ProblemClient";

export default async function ProblemPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const problem = await getProblem(slug);
  return <ProblemClient problem={problem} />;
}
