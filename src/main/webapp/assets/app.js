(() => {
  const qs = (selector, root = document) => root.querySelector(selector);
  const qsa = (selector, root = document) => Array.from(root.querySelectorAll(selector));

  // Demo account shortcuts on the login page.
  qsa('[data-fill-email]').forEach((button) => {
    button.addEventListener('click', () => {
      const email = qs('#email');
      const password = qs('#password');
      if (email && password) {
        email.value = button.dataset.fillEmail || '';
        password.value = button.dataset.fillPassword || '';
        password.focus();
      }
    });
  });

  // Show/hide password.
  const passwordToggle = qs('[data-toggle-password]');
  if (passwordToggle) {
    passwordToggle.addEventListener('click', () => {
      const input = qs('#password');
      if (!input) return;
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      passwordToggle.textContent = isPassword ? 'Hide' : 'Show';
      passwordToggle.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
    });
  }

  // Mobile sidebar.
  const menuToggle = qs('[data-menu-toggle]');
  const sidebar = qs('#sidebar');
  if (menuToggle && sidebar) {
    menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    document.addEventListener('click', (event) => {
      if (window.innerWidth <= 860 && sidebar.classList.contains('open') &&
          !sidebar.contains(event.target) && !menuToggle.contains(event.target)) {
        sidebar.classList.remove('open');
      }
    });
  }

  // Dismiss status messages after a short period.
  qsa('[data-auto-dismiss]').forEach((alert) => {
    window.setTimeout(() => {
      alert.style.transition = 'opacity .25s ease, transform .25s ease';
      alert.style.opacity = '0';
      alert.style.transform = 'translateY(-5px)';
      window.setTimeout(() => alert.remove(), 280);
    }, 5200);
  });

  // Filter the attendance roster without a server round trip.
  qsa('[data-table-search]').forEach((input) => {
    input.addEventListener('input', () => {
      const table = document.getElementById(input.dataset.tableSearch);
      if (!table) return;
      const query = input.value.trim().toLowerCase();
      qsa('[data-search-row]', table).forEach((row) => {
        row.hidden = !row.textContent.toLowerCase().includes(query);
      });
    });
  });

  // Bulk mark all currently listed students, then allow individual changes.
  qsa('[data-bulk-status]').forEach((button) => {
    button.addEventListener('click', () => {
      const value = button.dataset.bulkStatus;
      qsa('.status-select').forEach((select) => {
        select.value = value;
        updateStatusStyle(select);
      });
    });
  });

  function updateStatusStyle(select) {
    select.className = 'status-select status-select-' + select.value;
  }
  qsa('.status-select').forEach((select) => {
    select.addEventListener('change', () => updateStatusStyle(select));
  });
})();
