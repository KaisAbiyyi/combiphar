/**
 * Payment Transfer Page - JavaScript Handler
 * Menangani pemilihan bank dan menampilkan petunjuk transfer.
 */
(function() {
  'use strict';

  // DOM Elements
  const bankItems = document.querySelectorAll('.bank-item');
  const instructions = document.getElementById('transferInstructions');
  const closeBtn = document.getElementById('closeInstructions');
  const copyBtn = document.getElementById('copyAccountNumber');
  
  // Elements untuk menampilkan data bank terpilih
  const selectedBankName = document.getElementById('selectedBankName');
  const selectedAccountNumber = document.getElementById('selectedAccountNumber');
  const selectedAccountHolder = document.getElementById('selectedAccountHolder');
  const bankNameStep = document.getElementById('bankNameStep');

  let currentAccountNumber = '';

  /**
   * Menangani klik pada item bank.
   * @param {Event} e - Event click
   */
  function handleBankClick(e) {
    e.preventDefault();
    
    const button = e.currentTarget;
    const bankName = button.dataset.bank;
    const accountNumber = button.dataset.account;
    const accountHolder = button.dataset.holder;

    // Update UI dengan data bank terpilih
    if (selectedBankName) selectedBankName.textContent = bankName;
    if (selectedAccountNumber) selectedAccountNumber.textContent = accountNumber;
    if (selectedAccountHolder) selectedAccountHolder.textContent = 'a.n. ' + accountHolder;
    if (bankNameStep) bankNameStep.textContent = bankName;
    
    currentAccountNumber = accountNumber;

    // Hilangkan highlight dari semua bank
    bankItems.forEach(item => item.classList.remove('bank-item--active'));
    
    // Highlight bank yang dipilih
    button.classList.add('bank-item--active');

    // Tampilkan petunjuk transfer
    if (instructions) {
      instructions.style.display = 'block';
      instructions.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  /**
   * Menutup petunjuk transfer.
   */
  function closeInstructions() {
    if (instructions) {
      instructions.style.display = 'none';
    }
    // Hilangkan highlight dari semua bank
    bankItems.forEach(item => item.classList.remove('bank-item--active'));
  }

  /**
   * Menyalin nomor rekening ke clipboard.
   */
  function copyAccountNumber() {
    if (!currentAccountNumber) {
      showToast && showToast('Pilih bank terlebih dahulu', 'error');
      return;
    }

    navigator.clipboard.writeText(currentAccountNumber)
      .then(function() {
        showToast && showToast('Nomor rekening berhasil disalin!', 'success');
      })
      .catch(function(err) {
        console.error('Copy failed:', err);
        showToast && showToast('Gagal menyalin nomor rekening', 'error');
      });
  }

  // Event Listeners
  bankItems.forEach(function(item) {
    item.addEventListener('click', handleBankClick);
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', closeInstructions);
  }

  if (copyBtn) {
    copyBtn.addEventListener('click', copyAccountNumber);
  }

})();
