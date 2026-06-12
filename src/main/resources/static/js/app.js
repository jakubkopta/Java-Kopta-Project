const API = '/api';
const STORAGE_KEY = 'bookstore_auth';

const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const userBar = document.getElementById('user-bar');
const userInfo = document.getElementById('user-info');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const booksList = document.getElementById('books-list');
const reservationsList = document.getElementById('reservations-list');
const reservationsTitle = document.getElementById('reservations-title');
const reservationsHint = document.getElementById('reservations-hint');
const adminBookFormSection = document.getElementById('admin-book-form-section');
const bookForm = document.getElementById('book-form');
const bookFormTitle = document.getElementById('admin-form-title');
const bookFormSubmit = document.getElementById('book-form-submit');
const addBookBtn = document.getElementById('add-book-btn');
const cancelBookFormBtn = document.getElementById('cancel-book-form-btn');
const toast = document.getElementById('toast');

let auth = loadAuth();
let editingBookId = null;

document.querySelectorAll('.tab').forEach((tab) => {
  tab.addEventListener('click', () => switchTab(tab.dataset.tab));
});

loginForm.addEventListener('submit', handleLogin);
registerForm.addEventListener('submit', handleRegister);
bookForm.addEventListener('submit', handleBookFormSubmit);
document.getElementById('logout-btn').addEventListener('click', logout);
document.getElementById('refresh-books-btn').addEventListener('click', loadBooks);
document.getElementById('refresh-reservations-btn').addEventListener('click', loadReservations);
document.getElementById('search-btn').addEventListener('click', loadBooks);
document.getElementById('clear-search-btn').addEventListener('click', clearSearch);
addBookBtn.addEventListener('click', () => openBookForm());
cancelBookFormBtn.addEventListener('click', () => closeBookForm());
document.getElementById('search-input').addEventListener('keydown', (event) => {
  if (event.key === 'Enter') {
    event.preventDefault();
    loadBooks();
  }
});

init();

function init() {
  if (auth?.token) {
    showApp();
  } else {
    showAuth();
  }
}

function isAdmin() {
  return auth?.role === 'ADMIN';
}

function loadAuth() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY));
  } catch {
    return null;
  }
}

function saveAuth(data) {
  auth = data;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

function clearAuth() {
  auth = null;
  localStorage.removeItem(STORAGE_KEY);
}

function switchTab(tab) {
  document.querySelectorAll('.tab').forEach((button) => {
    button.classList.toggle('active', button.dataset.tab === tab);
  });
  loginForm.classList.toggle('hidden', tab !== 'login');
  registerForm.classList.toggle('hidden', tab !== 'register');
}

async function handleLogin(event) {
  event.preventDefault();
  const formData = new FormData(loginForm);
  await authenticate('/auth/login', {
    email: formData.get('email'),
    password: formData.get('password'),
  });
}

async function handleRegister(event) {
  event.preventDefault();
  const formData = new FormData(registerForm);
  await authenticate('/auth/register', {
    email: formData.get('email'),
    password: formData.get('password'),
  });
}

async function authenticate(path, body) {
  try {
    const response = await fetch(`${API}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || 'Authentication failed');
    }
    saveAuth(data);
    showToast(`Signed in as ${data.email}`, 'success');
    showApp();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function logout() {
  clearAuth();
  closeBookForm();
  showAuth();
  showToast('Logged out', 'success');
}

function showAuth() {
  authSection.classList.remove('hidden');
  appSection.classList.add('hidden');
  userBar.classList.add('hidden');
}

async function showApp() {
  authSection.classList.add('hidden');
  appSection.classList.remove('hidden');
  userBar.classList.remove('hidden');
  userInfo.innerHTML = `
    <span>${escapeHtml(auth.email)}</span>
    <span class="badge ${isAdmin() ? 'admin' : ''}">${auth.role}</span>
  `;

  if (isAdmin()) {
    addBookBtn.classList.remove('hidden');
    reservationsTitle.textContent = 'All reservations';
    reservationsHint.textContent = 'View every user reservation and return books on their behalf.';
    reservationsHint.classList.remove('hidden');
  } else {
    addBookBtn.classList.add('hidden');
    closeBookForm();
    reservationsTitle.textContent = 'My reservations';
    reservationsHint.classList.add('hidden');
  }

  await Promise.all([loadBooks(), loadReservations()]);
}

function openBookForm(book = null) {
  if (!isAdmin()) {
    return;
  }

  editingBookId = book?.id ?? null;
  bookFormTitle.textContent = book ? 'Edit book' : 'Add book';
  bookFormSubmit.textContent = book ? 'Save changes' : 'Add book';
  cancelBookFormBtn.classList.remove('hidden');
  adminBookFormSection.classList.remove('hidden');

  bookForm.title.value = book?.title ?? '';
  bookForm.author.value = book?.author ?? '';
  bookForm.isbn.value = book?.isbn ?? '';
  bookForm.totalCopies.value = book?.totalCopies ?? 1;
}

function closeBookForm() {
  editingBookId = null;
  bookForm.reset();
  bookForm.totalCopies.value = 1;
  adminBookFormSection.classList.add('hidden');
  cancelBookFormBtn.classList.add('hidden');
}

async function handleBookFormSubmit(event) {
  event.preventDefault();
  if (!isAdmin()) {
    return;
  }

  const formData = new FormData(bookForm);
  const payload = {
    title: formData.get('title'),
    author: formData.get('author'),
    isbn: formData.get('isbn'),
    totalCopies: Number(formData.get('totalCopies')),
  };

  try {
    if (editingBookId) {
      await apiFetch(`/books/${editingBookId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
      showToast('Book updated', 'success');
    } else {
      await apiFetch('/books', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      showToast('Book added', 'success');
    }

    closeBookForm();
    await loadBooks();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteBook(bookId, title) {
  if (!isAdmin()) {
    return;
  }

  if (!window.confirm(`Delete "${title}"?`)) {
    return;
  }

  try {
    await apiFetch(`/books/${bookId}`, { method: 'DELETE' });
    showToast('Book deleted', 'success');
    await Promise.all([loadBooks(), loadReservations()]);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (auth?.token) {
    headers.Authorization = `Bearer ${auth.token}`;
  }

  const response = await fetch(`${API}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearAuth();
    showAuth();
    throw new Error('Session expired. Please log in again.');
  }

  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || 'Request failed');
  }
  return data;
}

async function loadBooks() {
  booksList.innerHTML = '<div class="empty-state">Loading books…</div>';

  try {
    const query = document.getElementById('search-input').value.trim();
    const type = document.getElementById('search-type').value;
    const path = query
      ? `/books/search?q=${encodeURIComponent(query)}&type=${encodeURIComponent(type)}`
      : '/books';

    const books = await apiFetch(path);
    renderBooks(books);
  } catch (error) {
    booksList.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
    showToast(error.message, 'error');
  }
}

function renderBooks(books) {
  if (!books.length) {
    booksList.innerHTML = '<div class="empty-state">No books found.</div>';
    return;
  }

  booksList.innerHTML = books.map((book) => {
    const canReserve = auth.role === 'USER' && book.availableCopies > 0;
    const availabilityClass = book.availableCopies > 0 ? 'ok' : 'low';

    const adminActions = isAdmin() ? `
      <div class="btn-group">
        <button class="btn btn-ghost" data-edit-book='${escapeAttr(JSON.stringify(book))}'>Edit</button>
        <button class="btn btn-danger" data-delete-book-id="${book.id}" data-delete-book-title="${escapeAttr(book.title)}">Delete</button>
      </div>
    ` : '';

    const userAction = !isAdmin() ? `
      <button
        class="btn btn-secondary"
        data-book-id="${book.id}"
        ${canReserve ? '' : 'disabled'}
      >
        Reserve
      </button>
    ` : '';

    return `
      <article class="book-item">
        <h3>${escapeHtml(book.title)}</h3>
        <p class="book-meta">by ${escapeHtml(book.author)}</p>
        <p class="book-meta">ISBN: ${escapeHtml(book.isbn)}</p>
        <div class="book-actions">
          <span class="availability ${availabilityClass}">
            ${book.availableCopies} / ${book.totalCopies} available
          </span>
          <div class="book-actions-right">
            ${userAction}
            ${adminActions}
          </div>
        </div>
      </article>
    `;
  }).join('');

  booksList.querySelectorAll('[data-book-id]').forEach((button) => {
    button.addEventListener('click', () => reserveBook(Number(button.dataset.bookId)));
  });

  booksList.querySelectorAll('[data-edit-book]').forEach((button) => {
    button.addEventListener('click', () => {
      openBookForm(JSON.parse(button.dataset.editBook));
    });
  });

  booksList.querySelectorAll('[data-delete-book-id]').forEach((button) => {
    button.addEventListener('click', () => {
      deleteBook(Number(button.dataset.deleteBookId), button.dataset.deleteBookTitle);
    });
  });
}

async function reserveBook(bookId) {
  try {
    await apiFetch('/reservations', {
      method: 'POST',
      body: JSON.stringify({ bookId }),
    });
    showToast('Book reserved successfully', 'success');
    await Promise.all([loadBooks(), loadReservations()]);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function loadReservations() {
  reservationsList.innerHTML = '<div class="empty-state">Loading reservations…</div>';

  const path = isAdmin() ? '/reservations' : '/reservations/my';

  try {
    const reservations = await apiFetch(path);
    renderReservations(reservations, isAdmin());
  } catch (error) {
    reservationsList.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
    showToast(error.message, 'error');
  }
}

function renderReservations(reservations, adminView) {
  if (!reservations.length) {
    reservationsList.innerHTML = `<div class="empty-state">${adminView ? 'No reservations yet.' : 'You have no reservations yet.'}</div>`;
    return;
  }

  reservationsList.innerHTML = reservations.map((reservation) => {
    const canReturn = reservation.status === 'ACTIVE' || reservation.status === 'PENDING';
    const userLine = adminView
      ? `<p class="reservation-meta">User: ${escapeHtml(reservation.userEmail)}</p>`
      : '';

    return `
      <article class="reservation-item">
        <h3>${escapeHtml(reservation.bookTitle)}</h3>
        ${userLine}
        <p class="reservation-meta">Reserved on ${formatDate(reservation.createdAt)}</p>
        <div class="reservation-actions">
          <span class="status ${reservation.status}">${reservation.status}</span>
          <button
            class="btn btn-danger"
            data-reservation-id="${reservation.id}"
            ${canReturn ? '' : 'disabled'}
          >
            Return
          </button>
        </div>
      </article>
    `;
  }).join('');

  reservationsList.querySelectorAll('[data-reservation-id]').forEach((button) => {
    button.addEventListener('click', () => returnBook(Number(button.dataset.reservationId)));
  });
}

async function returnBook(reservationId) {
  try {
    await apiFetch(`/reservations/${reservationId}/return`, { method: 'POST' });
    showToast('Book returned', 'success');
    await Promise.all([loadBooks(), loadReservations()]);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function clearSearch() {
  document.getElementById('search-input').value = '';
  document.getElementById('search-type').value = 'ALL';
  loadBooks();
}

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function escapeAttr(value) {
  return escapeHtml(value).replaceAll('&#39;', '&apos;');
}

let toastTimer;

function showToast(message, type = 'success') {
  toast.textContent = message;
  toast.className = `toast ${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.add('hidden');
  }, 3200);
}
