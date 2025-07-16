/**
 * Notification system for BookBuddy
 * Handles notification badges for received and sent requests
 */

// Update notification badges
function updateNotificationBadges() {
    if (!isAuthenticated()) {
        return;
    }
    
    // Update received requests badge
    updateReceivedRequestsBadge();
    
    // Update sent requests badge
    updateSentRequestsBadge();
}

// Update received requests notification badge
function updateReceivedRequestsBadge() {
    fetch('/requests/api/notifications/received-count', { credentials: 'include' })
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('received-requests-badge');
            if (badge) {
                if (data.count > 0) {
                    badge.textContent = data.count;
                    badge.style.display = 'inline';
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error('Failed to update received requests badge:', error);
        });
}

// Update sent requests notification badge
function updateSentRequestsBadge() {
    fetch('/requests/api/notifications/sent-count', { credentials: 'include' })
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('sent-requests-badge');
            if (badge) {
                if (data.count > 0) {
                    badge.textContent = data.count;
                    badge.style.display = 'inline';
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error('Failed to update sent requests badge:', error);
        });
}

// Check if user is authenticated
function isAuthenticated() {
    return document.querySelector('.auth-required').style.display !== 'none';
}

// Initialize notifications
function initNotifications() {
    // Update badges immediately
    updateNotificationBadges();
    
    // Update badges every 30 seconds
    setInterval(updateNotificationBadges, 30000);
}

// Export functions for use in other scripts
window.notifications = {
    updateNotificationBadges,
    updateReceivedRequestsBadge,
    updateSentRequestsBadge,
    initNotifications
}; 