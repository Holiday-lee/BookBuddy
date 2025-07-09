
// Function to check authentication status using backend API
async function checkAuthStatus() {
    try {
        const response = await fetch('/api/current-user', {
            credentials: 'include'
        });
        if (response.ok) {
            const user = await response.json();
            return user && user.authenticated;
        }
        return false;
    } catch (error) {
        return false;
    }
}

// Function to show/hide navigation based on authentication
async function updateNavigation() {
    const isAuthenticated = await checkAuthStatus();
    const authRequired = document.querySelectorAll('.auth-required');
    const authButtons = document.getElementById('auth-buttons');
    
    if (isAuthenticated) {
        authRequired.forEach(element => element.style.display = 'block');
        if (authButtons) authButtons.style.display = 'none';
    } else {
        authRequired.forEach(element => element.style.display = 'none');
        if (authButtons) authButtons.style.display = 'block';
    }
}

// Function to handle authentication-protected actions
async function handleAuthProtection() {
    const isAuthenticated = await checkAuthStatus();
    const authProtectedLinks = document.querySelectorAll('.auth-protected');
    
    authProtectedLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            if (!isAuthenticated) {
                e.preventDefault();
                showLoginModal();
            }
        });
    });
}

// Function to show login modal
function showLoginModal() {
    const loginModal = document.getElementById('loginModal');
    if (loginModal) {
        const modal = new bootstrap.Modal(loginModal);
        modal.show();
    } else {
        // Fallback: redirect to login page
        window.location.href = '/pages/login.html';
    }
}

// Function to handle book detail views and swap requests
async function handleBookActions() {
    const isAuthenticated = await checkAuthStatus();
    
    // Handle book detail buttons
    const bookDetailButtons = document.querySelectorAll('.book-detail-btn, .swap-request-btn');
    bookDetailButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!isAuthenticated) {
                e.preventDefault();
                showLoginModal();
            }
        });
    });
}

// Function to protect entire pages that require authentication
async function protectPage(allowedPages = []) {
    const isAuthenticated = await checkAuthStatus();
    const currentPath = window.location.pathname;
    
    // Pages that require authentication
    const protectedPages = [
        '/pages/list-book.html',
        '/pages/my-received-requests.html',
        '/pages/my-sent-requests.html',
        '/pages/chat.html',
        '/pages/swap-history.html',
        '/pages/profile.html'
    ];
    
    // Check if current page is protected
    const isProtectedPage = protectedPages.some(page => currentPath.includes(page));
    
    if (isProtectedPage && !isAuthenticated) {
        // Store the intended destination
        localStorage.setItem('redirectAfterLogin', currentPath);
        // Redirect to login
        window.location.href = '/login';
    }
}

// Function to handle successful login
function handleSuccessfulLogin() {
    // Check if there's a redirect destination
    const redirectPath = localStorage.getItem('redirectAfterLogin');
    if (redirectPath) {
        localStorage.removeItem('redirectAfterLogin');
        window.location.href = redirectPath;
    } else {
        window.location.href = '/'; // Default redirect to home
    }
}

// Function to handle logout
function handleLogout() {
    localStorage.removeItem('redirectAfterLogin');
    window.location.href = '/logout';
}

// Initialize authentication when DOM is loaded
window.addEventListener('DOMContentLoaded', async function() {
    await updateNavigation();
    await handleAuthProtection();
    await handleBookActions();
    await protectPage();
    
    // Handle logout button if present
    const logoutBtn = document.querySelector('a[href="/logout"]');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            handleLogout();
        });
    }
});

// For testing purposes - you can remove these in production
// Function to simulate login (for testing)
function simulateLogin() {
    localStorage.setItem('isAuthenticated', 'true');
    location.reload();
}

// Function to simulate logout (for testing)
function simulateLogout() {
    localStorage.removeItem('isAuthenticated');
    location.reload();
}

// Make functions available globally for testing
window.simulateLogin = simulateLogin;
window.simulateLogout = simulateLogout;