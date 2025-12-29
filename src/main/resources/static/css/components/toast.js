/**
 * Simple toast notification system for cart operations.
 * Clean, minimal, defensive implementation.
 */
(function() {
  'use strict';

  // Create toast container if not exists
  function ensureToastContainer() {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    return container;
  }

  /**
   * Shows a toast notification
   * @param {string} message - The message to display
   * @param {string} type - 'success', 'error', or 'info'
   */
  window.showToast = function(message, type) {
    if (!message) return;
    
    const container = ensureToastContainer();
    const toast = document.createElement('div');
    toast.className = 'toast toast--' + (type || 'info');
    toast.textContent = message;
    
    container.appendChild(toast);
    
    // Trigger animation
    setTimeout(() => toast.classList.add('toast--show'), 10);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
      toast.classList.remove('toast--show');
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 300);
    }, 3000);
  };

  /**
   * Updates cart badge count in navbar
   * @param {number} count - Number of items in cart
   */
  window.updateCartBadge = function(count) {
    const cartLinks = document.querySelectorAll('.navbar__link[href="/cart"]');
    cartLinks.forEach(link => {
      // Remove existing badge
      const oldBadge = link.querySelector('.cart-badge');
      if (oldBadge) oldBadge.remove();
      
      if (count > 0) {
        const badge = document.createElement('span');
        badge.className = 'cart-badge';
        badge.textContent = count;
        link.appendChild(badge);
      }
    });
  };
})();
