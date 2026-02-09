<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HOA Portal - Maintenance</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen">

    <div id="login-section" class="flex items-center justify-center min-h-screen">
        <div class="bg-white p-8 rounded-lg shadow-md w-96">
            <h2 class="text-2xl font-bold mb-6 text-center text-blue-600">HOA Portal</h2>
            <input type="email" id="email" placeholder="Email" class="w-full p-2 mb-4 border rounded focus:ring-2 focus:ring-blue-500 outline-none">
            <input type="password" id="password" placeholder="Password" class="w-full p-2 mb-6 border rounded focus:ring-2 focus:ring-blue-500 outline-none">
            <button onclick="login()" class="w-full bg-blue-600 text-white p-2 rounded font-bold hover:bg-blue-700 transition">Login</button>
        </div>
    </div>

    <div id="dashboard-section" class="hidden">
        <nav class="bg-white shadow p-4 flex justify-between items-center">
            <h1 class="font-bold text-xl text-gray-800">HOA Maintenance Hub</h1>
            <div class="flex items-center gap-4">
                <span id="role-badge" class="px-2 py-1 bg-blue-100 text-blue-800 rounded text-[10px] uppercase font-black tracking-wider"></span>
                <button onclick="logout()" class="text-red-500 text-sm font-semibold hover:underline">Logout</button>
            </div>
        </nav>

        <div class="max-w-4xl mx-auto p-6">
            <div class="flex justify-between items-center mb-6">
                <h2 class="text-2xl font-bold text-gray-700">Service Requests</h2>
                <button onclick="toggleModal(true)" class="bg-green-600 text-white px-5 py-2 rounded-lg shadow-lg hover:bg-green-700 transition font-bold">
                    + Create Request
                </button>
            </div>
            <div id="request-list" class="grid gap-4"></div>
        </div>
    </div>

    <div id="modal" class="fixed inset-0 bg-black bg-opacity-60 hidden flex items-center justify-center p-4 z-50">
        <div class="bg-white p-6 rounded-xl w-full max-w-md shadow-2xl transform transition-all">
            <h3 class="text-xl font-bold mb-4 text-gray-800">New Maintenance Ticket</h3>
            <p class="text-xs text-gray-500 mb-4 italic">Note: Admin requests will be tagged as "Common Area".</p>
            <input type="text" id="new-title" placeholder="Summary (e.g. Broken streetlight)" class="w-full p-3 mb-3 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none">
            <textarea id="new-desc" placeholder="Detailed description..." class="w-full p-3 mb-4 border rounded-lg h-32 focus:ring-2 focus:ring-blue-500 outline-none"></textarea>
            <div class="flex justify-end gap-3">
                <button onclick="toggleModal(false)" class="px-4 py-2 text-gray-500 hover:bg-gray-100 rounded-lg">Cancel</button>
                <button onclick="submitRequest()" id="submit-btn" class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold">Submit</button>
            </div>
        </div>
    </div>

    <script>
        const API_BASE = '/portal';

        async function login() {
            const email = document.getElementById('email').value;
            const passwordHash = document.getElementById('password').value;
            try {
                const res = await fetch(`${API_BASE}/auth/login`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ email, passwordHash })
                });
                if (res.ok) {
                    const data = await res.json();
                    localStorage.setItem('hoa_token', data.token);
                    localStorage.setItem('hoa_role', data.role);
                    location.reload();
                } else { alert("Login failed. Check your credentials."); }
            } catch (e) { alert("Server unreachable."); }
        }

        function logout() { localStorage.clear(); location.reload(); }
        function toggleModal(show) { document.getElementById('modal').classList.toggle('hidden', !show); }

        async function fetchRequests() {
            const role = localStorage.getItem('hoa_role');
            const res = await fetch(`${API_BASE}/requests`, {
                headers: {'Authorization': `Bearer ${localStorage.getItem('hoa_token')}`}
            });
            const data = await res.json();
            const list = document.getElementById('request-list');
            
            const colors = {
                'pending': 'border-yellow-400',
                'in_progress': 'border-blue-400',
                'completed': 'border-green-400'
            };

            list.innerHTML = data.map(req => {
                const locationLabel = req.houseId === 0 ? "📍 Common Area" : `🏠 House #${req.houseId}`;
                
                return `
                <div class="bg-white p-5 rounded-lg shadow-sm border-l-8 ${colors[req.status] || 'border-gray-300'}">
                    <div class="flex justify-between items-start">
                        <div class="flex-1">
                            <div class="flex items-center gap-2 mb-1">
                                <h4 class="font-bold text-lg text-gray-800">${req.title}</h4>
                                <span class="text-[10px] bg-gray-100 px-2 py-0.5 rounded text-gray-500 font-bold border border-gray-200">${locationLabel}</span>
                            </div>
                            <p class="text-sm text-gray-600 mt-1">${req.description}</p>
                        </div>
                        <div class="flex flex-col items-end gap-3 ml-4">
                            ${role === 'admin' ? `
                                <div class="flex items-center gap-2">
                                    <select onchange="updateStatus(${req.id}, this.value)" class="text-sm border rounded p-1 bg-gray-50 focus:ring-2 focus:ring-blue-400">
                                        <option value="pending" ${req.status === 'pending' ? 'selected' : ''}>Pending</option>
                                        <option value="in_progress" ${req.status === 'in_progress' ? 'selected' : ''}>In Progress</option>
                                        <option value="completed" ${req.status === 'completed' ? 'selected' : ''}>Completed</option>
                                    </select>
                                    <button onclick="deleteRequest(${req.id})" class="text-red-400 hover:text-red-600 p-1" title="Delete">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                            ` : `
                                <span class="px-2 py-1 rounded text-[10px] font-black uppercase bg-gray-50 text-gray-500 border border-gray-200">
                                    ${req.status.replace('_', ' ')}
                                </span>
                            `}
                        </div>
                    </div>
                </div>
            `;}).join('');
        }

        async function submitRequest() {
            const btn = document.getElementById('submit-btn');
            const title = document.getElementById('new-title').value;
            const description = document.getElementById('new-desc').value;

            if(!title || !description) return alert("Please fill out all fields.");

            btn.disabled = true;
            btn.innerText = "Processing...";

            await fetch(`${API_BASE}/requests`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('hoa_token')}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ title, description })
            });
            
            toggleModal(false);
            document.getElementById('new-title').value = '';
            document.getElementById('new-desc').value = '';
            btn.disabled = false;
            btn.innerText = "Submit";
            fetchRequests();
        }

        async function updateStatus(id, newStatus) {
            await fetch(`${API_BASE}/requests/${id}/status`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('hoa_token')}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: newStatus })
            });
            fetchRequests();
        }

        async function deleteRequest(id) {
            if (!confirm("Delete this request permanently?")) return;
            await fetch(`${API_BASE}/requests/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${localStorage.getItem('hoa_token')}` }
            });
            fetchRequests();
        }

        window.onload = () => {
            if (localStorage.getItem('hoa_token')) {
                document.getElementById('login-section').classList.add('hidden');
                document.getElementById('dashboard-section').classList.remove('hidden');
                document.getElementById('role-badge').innerText = localStorage.getItem('hoa_role');
                fetchRequests();
            }
        };
    </script>
</body>
</html>