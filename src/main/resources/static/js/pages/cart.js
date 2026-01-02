/**
 * Cart page controls for quantity updates and item removal.
 */
(function() {
  'use strict';

  const quantityButtons = document.querySelectorAll('.cart-item__quantity-control');
  const removeForms = document.querySelectorAll('form[action="/api/cart/remove"]');
  const clearButton = document.getElementById('clearCartButton');

  function showError(message) {
    if (window.showToast) {
      showToast(message, 'error');
    }
  }

  function parseQuantity(text) {
    const value = Number.parseInt(String(text).trim(), 10);
    return Number.isFinite(value) ? value : null;
  }

  function postForm(url, payload) {
    const body = new URLSearchParams();
    Object.keys(payload).forEach(key => body.append(key, payload[key]));

    return fetch(url, {
      method: 'POST',
      body,
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(res => {
      // No content
      if (res.status === 204) return { success: true };
      const ct = (res.headers.get('content-type') || '').toLowerCase();
      if (ct.includes('application/json')) {
        return res.json().catch(() => ({ success: false, message: 'Respons tidak valid' }));
      }
      // Try to parse text as JSON if content-type is missing/incorrect, otherwise return text as message
      return res.text().then(text => {
        if (!text) return { success: false, message: 'Respons tidak valid' };
        try {
          return JSON.parse(text);
        } catch (e) {
          return { success: false, message: text };
        }
      });
    }).catch(() => ({ success: false, message: 'Respons tidak valid' }));
  }

  function updateQuantity(itemId, quantity) {
    return postForm('/api/cart/update', {
      itemId: itemId,
      quantity: String(quantity)
    });
  }

  function removeItem(itemId) {
    return postForm('/api/cart/remove', { itemId: itemId });
  }

  function clearCart() {
    return postForm('/api/cart/clear', {});
  }

  quantityButtons.forEach(button => {
    button.addEventListener('click', event => {
      event.preventDefault();
      const action = button.dataset.action;
      const itemId = button.dataset.itemid;

      if (!itemId) {
        showError('Item tidak valid.');
        return;
      }

      const container = button.closest('.cart-item');
      const quantityEl = container ? container.querySelector('.cart-item__quantity-value') : null;
      const currentQty = quantityEl ? parseQuantity(quantityEl.textContent) : null;

      if (currentQty == null) {
        showError('Kuantitas tidak terbaca.');
        return;
      }

      const delta = action === 'increase' ? 1 : -1;
      const nextQty = currentQty + delta;

      if (nextQty < 1) {
        showError('Kuantitas minimal 1.');
        return;
      }

      updateQuantity(itemId, nextQty).then(json => {
        if (json && json.success) {
          window.location.reload();
          return;
        }
        showError(json && json.message ? json.message : 'Gagal memperbarui kuantitas.');
      }).catch(() => {
        showError('Terjadi kesalahan saat memperbarui kuantitas.');
      });
    });
  });

  removeForms.forEach(form => {
    form.addEventListener('submit', event => {
      event.preventDefault();
      const itemInput = form.querySelector('input[name="itemId"]');
      const itemId = itemInput ? itemInput.value : '';

      if (!itemId) {
        showError('Item tidak valid.');
        return;
      }

      removeItem(itemId).then(json => {
        if (json && json.success) {
          window.location.reload();
          return;
        }
        showError(json && json.message ? json.message : 'Gagal menghapus item.');
      }).catch(() => {
        showError('Terjadi kesalahan saat menghapus item.');
      });
    });
  });

  if (clearButton) {
    clearButton.addEventListener('click', event => {
      event.preventDefault();
      clearCart().then(json => {
        if (json && json.success) {
          window.location.reload();
          return;
        }
        showError(json && json.message ? json.message : 'Gagal mengosongkan keranjang.');
      }).catch(() => {
        showError('Terjadi kesalahan saat mengosongkan keranjang.');
      });
    });
  }

  // Delegated blur listener to save per-item notes silently (no notifications)
  document.addEventListener('blur', function (e) {
    const area = e.target;
    if (!area || !area.classList || !area.classList.contains('cart-item__note')) return;

    const itemId = area.dataset.itemid;
    const note = area.value || '';

    if (!itemId) return;

    // Save silently in background without showing any notifications
    postForm('/api/cart/update-note', { itemId: itemId, note: note });
  }, true);
})();
