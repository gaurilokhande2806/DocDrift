const API_BASE = '/api';
let currentFindings = [];

document.addEventListener('DOMContentLoaded', () => {
    loadProjects();
});

async function loadProjects() {
    try {
        const res = await fetch(`${API_BASE}/projects`);
        const projects = await res.json();
        const select = document.getElementById('projectSelect');
        select.innerHTML = '';

        if (projects.length === 0) {
            select.innerHTML = '<option value="">No projects registered</option>';
            return;
        }

        projects.forEach(p => {
            const opt = document.createElement('option');
            opt.value = p.id;
            opt.textContent = `${p.name} (${p.rootPath})`;
            select.appendChild(opt);
        });

        loadProjectData();
    } catch (err) {
        console.error('Failed to load projects:', err);
    }
}

async function loadProjectData() {
    const projectId = document.getElementById('projectSelect').value;
    if (!projectId) return;

    try {
        const res = await fetch(`${API_BASE}/projects/${projectId}/runs/latest`);
        if (res.status === 404) {
            resetDashboardUI();
            return;
        }
        const data = await res.json();
        renderDashboard(data);
    } catch (err) {
        console.error('Failed to load project run:', err);
    }
}

function renderDashboard(data) {
    // Score
    const dhs = data.overallDhs != null ? data.overallDhs.toFixed(1) : '--';
    document.getElementById('dhsScoreValue').textContent = dhs;

    const circle = document.getElementById('dhsScoreCircle');
    const badge = document.getElementById('decayBadge');

    let bgClass = 'bg-healthy';
    let label = 'HEALTHY';

    if (data.overallDhs < 50) {
        bgClass = 'bg-severe';
        label = 'SEVERE DECAY';
    } else if (data.overallDhs < 75) {
        bgClass = 'bg-moderate';
        label = 'MODERATE DECAY';
    } else if (data.overallDhs < 90) {
        bgClass = 'bg-mild';
        label = 'MILD DECAY';
    }

    circle.style.borderColor = getComputedStyle(document.documentElement).getPropertyValue(`--bs-${bgClass.replace('bg-', '')}`) || '#0d6efd';
    badge.className = `badge rounded-pill ${bgClass} fs-6 px-3 py-2`;
    badge.textContent = label;

    // Stats
    document.getElementById('statElements').textContent = data.totalVerifiableElements || 0;
    document.getElementById('statFindings').textContent = data.totalFindings || 0;

    // Render Findings
    currentFindings = data.findings || [];
    applyFilters();
}

function resetDashboardUI() {
    document.getElementById('dhsScoreValue').textContent = '--';
    document.getElementById('decayBadge').textContent = 'NOT ANALYZED';
    document.getElementById('statElements').textContent = '0';
    document.getElementById('statFindings').textContent = '0';
    document.getElementById('findingsTableBody').innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No analysis runs yet. Click "Run Analysis".</td></tr>';
}

async function runAnalysis() {
    const projectId = document.getElementById('projectSelect').value;
    if (!projectId) {
        alert('Please select or register a project first.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/projects/${projectId}/analyze`, { method: 'POST' });
        const data = await res.json();
        renderDashboard(data);
    } catch (err) {
        alert('Analysis failed: ' + err.message);
    }
}

function applyFilters() {
    const sevFilter = document.getElementById('filterSeverity').value;
    const catFilter = document.getElementById('filterCategory').value;

    let filtered = currentFindings;

    if (sevFilter !== 'ALL') {
        filtered = filtered.filter(f => f.severity === sevFilter);
    }
    if (catFilter !== 'ALL') {
        filtered = filtered.filter(f => f.category === catFilter);
    }

    renderFindingsTable(filtered);
}

function renderFindingsTable(findings) {
    const tbody = document.getElementById('findingsTableBody');
    tbody.innerHTML = '';

    if (findings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No matching findings found.</td></tr>';
        return;
    }

    findings.forEach(f => {
        const tr = document.createElement('tr');

        const categoryTitle = f.category ? `${f.category}` : 'DC-00';
        const sevClass = f.severity === 'CRITICAL' ? 'bg-danger' :
                         f.severity === 'HIGH' ? 'bg-warning text-dark' :
                         f.severity === 'MEDIUM' ? 'bg-info text-dark' : 'bg-secondary';

        tr.innerHTML = `
            <td><span class="fw-bold">${categoryTitle}</span></td>
            <td><span class="badge ${sevClass}">${f.severity}</span></td>
            <td>
                <div class="small fw-semibold text-muted">${f.docLocation || 'N/A'}</div>
                <div class="diff-box text-dark">${escapeHtml(f.documentedValue || '')}</div>
            </td>
            <td>
                <div class="small fw-semibold text-muted">${f.codeLocation || 'N/A'}</div>
                <div class="diff-box text-dark">${escapeHtml(f.actualValue || '')}</div>
            </td>
            <td>
                <div class="small text-success fw-bold">${escapeHtml(f.difference || '')}</div>
                <div class="small text-secondary">${escapeHtml(f.suggestion || '')}</div>
            </td>
            <td>
                <select class="form-select form-select-sm" onchange="updateFindingStatus(${f.id}, this.value)">
                    <option value="OPEN" ${f.status === 'OPEN' ? 'selected' : ''}>OPEN</option>
                    <option value="FALSE_POSITIVE" ${f.status === 'FALSE_POSITIVE' ? 'selected' : ''}>FALSE POSITIVE</option>
                    <option value="ACCEPTED" ${f.status === 'ACCEPTED' ? 'selected' : ''}>ACCEPTED</option>
                </select>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function updateFindingStatus(findingId, status) {
    try {
        await fetch(`${API_BASE}/findings/${findingId}/status?status=${status}`, { method: 'POST' });
        loadProjectData();
    } catch (err) {
        console.error('Status update failed:', err);
    }
}

async function registerProject() {
    const name = document.getElementById('regName').value;
    const path = document.getElementById('regPath').value;
    const desc = document.getElementById('regDesc').value;

    if (!name || !path) {
        alert('Please fill in Name and Root Path.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/projects/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, rootPath: path, description: desc })
        });
        if (!res.ok) {
            const errData = await res.json();
            alert('Registration error: ' + (errData.message || res.statusText));
            return;
        }
        const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
        modal.hide();
        loadProjects();
    } catch (err) {
        alert('Failed to register project: ' + err.message);
    }
}

function exportReport(format) {
    const projectId = document.getElementById('projectSelect').value;
    if (!projectId) return;
    window.open(`${API_BASE}/projects/${projectId}/export/${format}`, '_blank');
}

function escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
