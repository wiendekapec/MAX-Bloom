/**
 * NewPlanScreen — создание тарифа
 * US-2.1, US-1.4 (self-employed confirmation before first plan)
 */

import { useState } from 'react';
import { TopBar, MainBtn, InfoBanner } from '../components/ui';
import { useNav } from '../contexts/NavContext';
import { useToast } from '../contexts/ToastContext';
import { api, type PlanPeriod } from '../lib/api';
import { hapticLight, hapticMedium, hapticSuccess, hapticError } from '../lib/maxBridge';

const PERIOD_OPTIONS: { label: string; value: PlanPeriod }[] = [
  { label: '7 дней', value: 7 },
  { label: '30 дней', value: 30 },
  { label: '90 дней', value: 90 },
  { label: 'Разово', value: 0 },
];

interface FormState {
  title: string;
  description: string;
  priceRub: string;
  periodDays: PlanPeriod;
  selfEmployedConfirmed: boolean;
}

interface FormErrors {
  title?: string;
  priceRub?: string;
  selfEmployed?: string;
}

export default function NewPlanScreen() {
  const { goBack } = useNav();
  const { showToast } = useToast();

  const [form, setForm] = useState<FormState>({
    title: '',
    description: '',
    priceRub: '',
    periodDays: 30,
    selfEmployedConfirmed: false,
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);

  const validate = (): boolean => {
    const e: FormErrors = {};
    if (!form.title.trim() || form.title.length > 128) {
      e.title = 'Введите название (до 128 символов)';
    }
    const price = parseFloat(form.priceRub);
    if (!form.priceRub || isNaN(price) || price <= 0 || price > 999999) {
      e.priceRub = 'Введите корректную цену в рублях';
    }
    if (!form.selfEmployedConfirmed) {
      e.selfEmployed = 'Необходимо подтвердить статус перед активацией тарифа';
    }
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) {
      hapticError();
      return;
    }
    hapticMedium();
    setLoading(true);
    try {
      await api.createPlan({
        title: form.title.trim(),
        description: form.description.trim() || undefined,
        priceRub: parseFloat(form.priceRub),
        periodDays: form.periodDays,
        selfEmployedConfirmed: form.selfEmployedConfirmed,
      });
      hapticSuccess();
      showToast('Тариф создан!', 'success');
      goBack();
    } catch (err: unknown) {
      hapticError();
      const e = err as { message?: string };
      showToast(e?.message ?? 'Ошибка создания тарифа', 'error');
    } finally {
      setLoading(false);
    }
  };

  const set = (key: keyof FormState) => (val: string | boolean | PlanPeriod) =>
    setForm((prev) => ({ ...prev, [key]: val }));

  return (
    <div className="screen fade-in">
      <TopBar title="Новый тариф" onBack={goBack} />

      <div style={{ paddingTop: 8 }}>
        {/* Channel binding reminder */}
        <InfoBanner type="info" icon="📡">
          После создания тарифа добавьте бота администратором в ваш MAX-канал с правом{' '}
          <strong>«Управление участниками»</strong>, чтобы бот мог выдавать и отзывать доступ.
        </InfoBanner>

        {/* Title */}
        <div className="field">
          <label htmlFor="plan-title">Название тарифа *</label>
          <input
            id="plan-title"
            className={`field-input ${errors.title ? 'error' : ''}`}
            type="text"
            placeholder="Например: Pro с менторством"
            value={form.title}
            maxLength={128}
            onChange={(e) => set('title')(e.target.value)}
          />
          {errors.title && <div className="field-error">{errors.title}</div>}
        </div>

        {/* Description */}
        <div className="field">
          <label htmlFor="plan-desc">Описание (необязательно)</label>
          <textarea
            id="plan-desc"
            className="field-input"
            placeholder="Что включает тариф?"
            value={form.description}
            maxLength={500}
            rows={3}
            style={{ resize: 'none', lineHeight: 1.5 }}
            onChange={(e) => set('description')(e.target.value)}
          />
        </div>

        {/* Price */}
        <div className="field">
          <label htmlFor="plan-price">Цена, ₽ *</label>
          <input
            id="plan-price"
            className={`field-input ${errors.priceRub ? 'error' : ''}`}
            type="number"
            inputMode="numeric"
            placeholder="990"
            value={form.priceRub}
            min={1}
            max={999999}
            onChange={(e) => set('priceRub')(e.target.value)}
          />
          {errors.priceRub && <div className="field-error">{errors.priceRub}</div>}
        </div>

        {/* Period */}
        <div className="field">
          <label>Период доступа *</label>
          <div className="period-chips">
            {PERIOD_OPTIONS.map((opt) => (
              <div
                key={opt.value}
                id={`period-${opt.value}`}
                className={`chip ${form.periodDays === opt.value ? 'active' : ''}`}
                onClick={() => {
                  hapticLight();
                  set('periodDays')(opt.value);
                }}
              >
                {opt.label}
              </div>
            ))}
          </div>
        </div>

        {/* Self-employed declaration — US-1.4 */}
        <div style={{
          background: 'var(--glass)',
          border: '1.5px solid var(--glass-b)',
          borderRadius: 'var(--radius)',
          padding: '14px 16px',
          marginBottom: 8,
        }}>
          <div style={{ fontSize: 12, color: 'var(--muted)', marginBottom: 10, lineHeight: 1.5 }}>
            ⚖️ <strong style={{ color: 'var(--text)' }}>Подтверждение статуса</strong>
            <br />
            Согласно ФЗ-54, приём платежей требует наличия юридического статуса. Без подтверждения
            тариф не может быть активирован.
          </div>
          <div className="checkbox-row" style={{ padding: 0 }}>
            <input
              id="self-employed-confirm"
              type="checkbox"
              checked={form.selfEmployedConfirmed}
              onChange={(e) => set('selfEmployedConfirmed')(e.target.checked)}
            />
            <label htmlFor="self-employed-confirm" className="checkbox-label">
              Подтверждаю, что зарегистрирован как самозанятый / ИП / ООО и несу ответственность за
              легальность приёма платежей. Настоящее подтверждение является декларацией.{' '}
              <a href="https://npd.nalog.ru" target="_blank" rel="noreferrer">
                npd.nalog.ru →
              </a>
            </label>
          </div>
          {errors.selfEmployed && (
            <div className="field-error" style={{ marginTop: 8 }}>
              {errors.selfEmployed}
            </div>
          )}
        </div>

        {/* Sandbox notice */}
        <div style={{ fontSize: 11.5, color: 'var(--muted)', textAlign: 'center', padding: '8px 0 24px', lineHeight: 1.5 }}>
          Оплата через ЮKassa используется в тестовом режиме (sandbox).{'\n'}
          Реальные списания не происходят.
        </div>
      </div>

      <MainBtn
        label="Создать тариф"
        onClick={handleSubmit}
        loading={loading}
      />
    </div>
  );
}
