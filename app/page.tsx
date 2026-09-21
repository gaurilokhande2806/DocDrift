'use client';

import React, { useState, useEffect } from 'react';
import { Play, Download, FolderPlus, ShieldCheck, AlertTriangle, FileCode, CheckCircle } from 'lucide-react';
import { HealthScoreReport, Finding } from '@/lib/types';

export default function Dashboard() {
  const [report, setReport] = useState<HealthScoreReport | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');

  useEffect(() => {
    handleRunAnalysis();
  }, []);

  const handleRunAnalysis = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/analyze', { method: 'POST' });
      const data = await res.json();
      setReport(data);
    } catch (err) {
      console.error('Failed to run analysis:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleExportMarkdown = () => {
    window.open('/api/export', '_blank');
  };

  const filteredFindings = report?.findings.filter(f => {
    if (severityFilter === 'ALL') return true;
    return f.severity === severityFilter;
  }) || [];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      {/* Navbar */}
      <nav className="border-b bg-slate-900 px-6 py-4 text-white shadow-sm">
        <div className="mx-auto flex max-w-7xl items-center justify-between">
          <div className="flex items-center space-x-3">
            <ShieldCheck className="h-7 w-7 text-indigo-400" />
            <span className="text-xl font-bold tracking-tight">DocDrift</span>
            <span className="rounded bg-indigo-500/20 px-2 py-0.5 text-xs text-indigo-300 font-medium">Vercel Production Ready</span>
          </div>
          <div className="text-xs text-slate-400">Documentation Decay Detector v1.0.0</div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-6 py-8">

        {/* Top Action Bar */}
        <div className="mb-6 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <div className="flex items-center space-x-4">
            <span className="font-semibold text-slate-700">Target Project:</span>
            <select className="rounded-lg border border-slate-300 bg-slate-50 px-4 py-2 text-sm font-medium text-slate-800">
              <option>Demo Spring Boot Store (samples/demo-project)</option>
            </select>
            <button
              onClick={handleRunAnalysis}
              disabled={loading}
              className="inline-flex items-center space-x-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:opacity-50"
            >
              <Play className="h-4 w-4" />
              <span>{loading ? 'Analyzing...' : 'Run Consistency Scan'}</span>
            </button>
          </div>
          <div className="flex items-center space-x-3">
            <button
              onClick={handleExportMarkdown}
              className="inline-flex items-center space-x-2 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50"
            >
              <Download className="h-4 w-4" />
              <span>Export Report</span>
            </button>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="mb-8 grid gap-6 md:grid-cols-3">
          {/* DHS Gauge */}
          <div className="flex flex-col items-center justify-center rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">Documentation Health Score</h3>
            <div className="relative flex h-32 w-32 items-center justify-center rounded-full border-8 border-indigo-600 bg-slate-50 shadow-inner">
              <span className="text-4xl font-extrabold text-slate-800">{report?.overallDhs ?? '--'}</span>
              <span className="absolute bottom-4 text-xs font-semibold text-slate-400">/ 100</span>
            </div>
            <span className="mt-4 inline-block rounded-full bg-indigo-100 px-4 py-1 text-xs font-bold text-indigo-700">
              {report?.decayLevel ?? 'ANALYZING'}
            </span>
          </div>

          {/* Stat Cards */}
          <div className="flex flex-col justify-between space-y-4 md:col-span-2">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="rounded-xl border border-indigo-100 border-l-4 border-l-indigo-600 bg-white p-5 shadow-sm">
                <span className="text-xs font-bold uppercase text-slate-400">Verifiable Elements</span>
                <div className="mt-2 text-3xl font-extrabold text-slate-800">{report?.totalVerifiableElements ?? 0}</div>
                <div className="mt-1 text-xs text-slate-500">Extracted across Code, Docs, DB</div>
              </div>
              <div className="rounded-xl border border-amber-100 border-l-4 border-l-amber-500 bg-white p-5 shadow-sm">
                <span className="text-xs font-bold uppercase text-slate-400">Inconsistencies Detected</span>
                <div className="mt-2 text-3xl font-extrabold text-amber-600">{report?.totalFindings ?? 0}</div>
                <div className="mt-1 text-xs text-slate-500">Categories DC-01 to DC-10</div>
              </div>
            </div>

            {/* Subscores */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
              <h4 className="mb-3 text-xs font-bold uppercase tracking-wider text-slate-400">Consistency Breakdown</h4>
              <div className="grid grid-cols-2 gap-4 text-xs">
                <div>
                  <span className="text-slate-600 font-medium">API Consistency</span>
                  <div className="mt-1 h-2 w-full rounded-full bg-slate-100">
                    <div className="h-2 rounded-full bg-emerald-500" style={{ width: '82.5%' }}></div>
                  </div>
                </div>
                <div>
                  <span className="text-slate-600 font-medium">README Consistency</span>
                  <div className="mt-1 h-2 w-full rounded-full bg-slate-100">
                    <div className="h-2 rounded-full bg-indigo-500" style={{ width: '89%' }}></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Findings Table */}
        <div className="rounded-xl border border-slate-200 bg-white shadow-sm overflow-hidden">
          <div className="flex items-center justify-between border-b bg-slate-50 px-6 py-4">
            <h3 className="font-bold text-slate-800">Detected Documentation Inconsistencies</h3>
            <div className="flex items-center space-x-3">
              <span className="text-xs text-slate-500 font-medium">Filter Severity:</span>
              <select
                value={severityFilter}
                onChange={(e) => setSeverityFilter(e.target.value)}
                className="rounded-md border border-slate-300 bg-white px-3 py-1 text-xs font-medium text-slate-700"
              >
                <option value="ALL">All Severities</option>
                <option value="CRITICAL">Critical</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-700">
              <thead className="border-b bg-slate-100/70 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-6 py-3 font-semibold">Category</th>
                  <th className="px-6 py-3 font-semibold">Severity</th>
                  <th className="px-6 py-3 font-semibold">Documented Location & Value</th>
                  <th className="px-6 py-3 font-semibold">Actual Code Implementation</th>
                  <th className="px-6 py-3 font-semibold">Suggested Fix</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {filteredFindings.map((f) => (
                  <tr key={f.id} className="hover:bg-slate-50/80 transition">
                    <td className="px-6 py-4 font-bold text-slate-900">{f.category}</td>
                    <td className="px-6 py-4">
                      <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-bold ${
                        f.severity === 'CRITICAL' ? 'bg-rose-100 text-rose-700' :
                        f.severity === 'HIGH' ? 'bg-amber-100 text-amber-700' : 'bg-sky-100 text-sky-700'
                      }`}>
                        {f.severity}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-xs text-slate-400 font-mono mb-1">{f.docLocation}</div>
                      <div className="rounded bg-slate-100 px-2 py-1 font-mono text-xs text-slate-800">{f.documentedValue}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-xs text-slate-400 font-mono mb-1">{f.codeLocation}</div>
                      <div className="rounded bg-slate-100 px-2 py-1 font-mono text-xs text-slate-800">{f.actualValue}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-xs font-semibold text-emerald-600 mb-1">{f.difference}</div>
                      <div className="text-xs text-slate-500">{f.suggestion}</div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

      </main>
    </div>
  );
}
