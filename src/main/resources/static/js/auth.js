
// Function to check authentication status
function checkAuthStatus() {
    // This would typically make an API call to your backend
    // For now, you can manually test by changing this value or checking localStorage
    const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true' || false;
    return isAuthenticated;
}

// Function to show/hide navigation based on authentication
function updateNavigation() {
    const isAuthenticated = checkAuthStatus();
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
function handleAuthProtection() {
    const isAuthenticated = checkAuthStatus();
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
function handleBookActions() {
    const isAuthenticated = checkAuthStatus();
    
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
function protectPage(allowedPages = []) {
    const isAuthenticated = checkAuthStatus();
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
        window.location.href = '/pages/login.html';
    }
}

// Function to handle successful login
function handleSuccessfulLogin() {
    localStorage.setItem('isAuthenticated', 'true');
    
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
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('redirectAfterLogin');
    window.location.href = '/';
}

// Initialize authentication when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    updateNavigation();
    handleAuthProtection();
    handleBookActions();
    protectPage();
    
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