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
    
    // Update chat badge
    updateChatBadge();
}

// Update received requests notification badge
function updateReceivedRequestsBadge() {
    fetch('/requests/api/notifications/received-count-new', { credentials: 'include' })
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
    fetch('/requests/api/notifications/sent-count-new', { credentials: 'include' })
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

// Update chat notification badge
function updateChatBadge() {
    fetch('/api/chat/notifications/unread-count', { credentials: 'include' })
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('chat-badge');
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
            console.error('Failed to update chat badge:', error);
        });
}

// Mark sent requests as seen (call this when user visits sent requests page)
function markSentRequestsAsSeen() {
    fetch('/requests/api/notifications/mark-sent-seen', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            // Update the badge immediately to hide it
            const badge = document.getElementById('sent-requests-badge');
            if (badge) {
                badge.style.display = 'none';
            }
        }
    })
    .catch(error => {
        console.error('Failed to mark sent requests as seen:', error);
    });
}

// Mark received requests as seen (call this when user visits received requests page)
function markReceivedRequestsAsSeen() {
    fetch('/requests/api/notifications/mark-received-seen', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            // Update the badge immediately to hide it
            const badge = document.getElementById('received-requests-badge');
            if (badge) {
                badge.style.display = 'none';
            }
        }
    })
    .catch(error => {
        console.error('Failed to mark received requests as seen:', error);
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
    updateChatBadge,
    markReceivedRequestsAsSeen,
    markSentRequestsAsSeen,
    initNotifications
}; 