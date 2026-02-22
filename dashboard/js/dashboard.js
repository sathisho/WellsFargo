/**
 * WellsFargo Automation Dashboard — JavaScript
 * Reads JSON from ../test-results/ and renders the dashboard.
 */

document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});

async function loadDashboard() {
    try {
        // Load summary and meta-info
        const [summary, meta] = await Promise.all([
            fetchJSON('../test-results/summary.json'),
            fetchJSON('../test-results/meta-info.json')
        ]);

        renderSummary(summary);
        renderProgressBar(summary);
        renderScenarioTable(summary.scenarios || []);
        renderMetaInfo(meta);

    } catch (error) {
        console.warn('Dashboard data not yet available:', error.message);
        document.querySelector('main').innerHTML = `
            <div style="text-align:center; padding:80px 20px;">
                <h2>📊 No Test Results Yet</h2>
                <p style="color:#666; margin-top:12px;">
                    Run your tests first, then refresh this dashboard.<br>
                    <code style="background:#f0f0f0; padding:4px 12px; border-radius:4px; margin-top:8px; display:inline-block;">
                        mvn clean test -Dcucumber.filter.tags="@Smoke" -Dexecution.mode=local
                    </code>
                </p>
            </div>
        `;
    }
}

async function fetchJSON(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}: ${url}`);
    return response.json();
}

function renderSummary(summary) {
    document.getElementById('total-count').textContent = summary.total || 0;
    document.getElementById('pass-count').textContent = summary.passed || 0;
    document.getElementById('fail-count').textContent = summary.failed || 0;
    document.getElementById('skip-count').textContent = summary.skipped || 0;
    document.getElementById('pass-rate').textContent = summary.pass_rate || '0%';
    document.getElementById('duration').textContent = summary.duration_readable || '0s';
}

function renderProgressBar(summary) {
    const total = summary.total || 1;
    const passPct = ((summary.passed || 0) / total * 100).toFixed(1);
    const failPct = ((summary.failed || 0) / total * 100).toFixed(1);
    const skipPct = ((summary.skipped || 0) / total * 100).toFixed(1);

    document.getElementById('progress-pass').style.width = passPct + '%';
    document.getElementById('progress-fail').style.width = failPct + '%';
    document.getElementById('progress-skip').style.width = skipPct + '%';
}

function renderScenarioTable(scenarios) {
    const tbody = document.getElementById('scenario-body');
    tbody.innerHTML = '';

    if (scenarios.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#999;">No scenarios executed yet</td></tr>';
        return;
    }

    scenarios.forEach((s, i) => {
        const statusClass = s.status.toUpperCase().includes('PASS') ? 'status-pass'
                          : s.status.toUpperCase().includes('FAIL') ? 'status-fail'
                          : 'status-skip';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${i + 1}</td>
            <td>${s.feature || '-'}</td>
            <td>${s.scenario || '-'}</td>
            <td class="${statusClass}">${s.status.toUpperCase()}</td>
            <td>${formatDuration(s.duration_ms)}</td>
            <td>${s.error || '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function renderMetaInfo(meta) {
    if (!meta) return;

    document.getElementById('env-badge').textContent = meta.environment || 'N/A';
    document.getElementById('exec-date').textContent = formatDate(meta.execution_date);

    const container = document.getElementById('meta-details');
    container.innerHTML = `
        <div class="meta-grid">
            <div class="meta-item">
                <label>Framework</label>
                <span>${meta.framework || 'N/A'}</span>
            </div>
            <div class="meta-item">
                <label>Environment</label>
                <span>${meta.environment || 'N/A'}</span>
            </div>
            <div class="meta-item">
                <label>Browser</label>
                <span>${meta.browser || 'N/A'}</span>
            </div>
            <div class="meta-item">
                <label>Java Version</label>
                <span>${meta.java_version || 'N/A'}</span>
            </div>
            <div class="meta-item">
                <label>OS</label>
                <span>${meta.os || 'N/A'}</span>
            </div>
            <div class="meta-item">
                <label>Result</label>
                <span style="color: ${meta.result === 'PASS' ? '#28a745' : '#dc3545'}; font-weight:700;">
                    ${meta.result || 'N/A'}
                </span>
            </div>
        </div>
    `;
}

function formatDuration(ms) {
    if (!ms) return '-';
    if (ms < 1000) return ms + 'ms';
    const seconds = Math.round(ms / 1000);
    if (seconds < 60) return seconds + 's';
    return Math.floor(seconds / 60) + 'm ' + (seconds % 60) + 's';
}

function formatDate(isoString) {
    if (!isoString) return '';
    try {
        const date = new Date(isoString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch {
        return isoString;
    }
}
