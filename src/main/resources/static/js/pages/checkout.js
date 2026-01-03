/**
 * Checkout page interactions: refresh order summary when courier changes.
 */
(function() {
  'use strict';

  const courierSelect = document.getElementById('courier');
  const subtotalEl = document.querySelector('[data-summary="subtotal"]');
  const shippingEl = document.querySelector('[data-summary="shipping"]');
  const totalEl = document.querySelector('[data-summary="total"]');
  const currency = new Intl.NumberFormat('id-ID');

  function formatCurrency(value) {
    const number = Number(value);
    if (Number.isNaN(number)) {
      return 'Rp 0';
    }
    return 'Rp ' + currency.format(number);
  }

  function updateSummary(summary) {
    if (subtotalEl) subtotalEl.textContent = formatCurrency(summary.subtotal);
    if (shippingEl) shippingEl.textContent = formatCurrency(summary.shippingCost);
    if (totalEl) totalEl.textContent = formatCurrency(summary.totalPrice);
  }

  function fetchSummary() {
    if (!courierSelect) return;
    const data = new FormData();
    data.append('courier', courierSelect.value);

    fetch('/api/checkout/calculate', {
      method: 'POST',
      body: data,
      credentials: 'same-origin'
    })
      .then(res => res.json())
      .then(json => {
        if (json && json.success) {
          updateSummary(json);
          return;
        }
        const message = json && json.message ? json.message : 'Gagal menghitung ringkasan';
        window.showToast && showToast(message, 'error');
      })
      .catch(() => {
        window.showToast && showToast('Terjadi kesalahan saat menghitung ringkasan', 'error');
      });
  }

  if (courierSelect) {
    courierSelect.addEventListener('change', fetchSummary);
    fetchSummary();
  }

  // Form submit normal (tidak perlu AJAX, langsung redirect)
})();
