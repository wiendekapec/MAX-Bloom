/**
 * TokenExpiredPage — страница для /i/{token} когда токен устарел
 * US-5.2 — redirect endpoint error state
 * Это отдельная HTML-страница (не мини-апп), открывается в браузере
 */

export default function TokenExpiredPage() {
  const BOT_LINK = 'https://max.ru/MaxBloomBot';

  return (
    <div style={{
      minHeight: '100vh',
      background: 'radial-gradient(ellipse 80% 60% at 50% -20%, #152C22 0%, #0D1E18 70%)',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: "'Manrope', system-ui, sans-serif",
      color: '#F0F5F2',
      padding: '40px 24px',
      textAlign: 'center',
    }}>
      <div style={{
        width: 80,
        height: 80,
        borderRadius: '50%',
        background: 'rgba(255,107,107,0.15)',
        border: '2px solid rgba(255,107,107,0.3)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: 36,
        marginBottom: 24,
      }}>
        🔐
      </div>

      <h1 style={{
        fontFamily: "'Unbounded', sans-serif",
        fontSize: 22,
        fontWeight: 700,
        marginBottom: 12,
      }}>
        Ссылка устарела
      </h1>

      <p style={{
        fontSize: 14,
        color: '#7A9990',
        lineHeight: 1.7,
        maxWidth: 300,
        marginBottom: 32,
      }}>
        Эта одноразовая ссылка уже использована или срок её действия истёк (24 часа).
        <br /><br />
        Если у вас активная подписка, запросите новую ссылку прямо в боте.
      </p>

      <a
        href={BOT_LINK}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 8,
          padding: '15px 28px',
          background: '#FF6F8E',
          color: '#1A0D12',
          borderRadius: 18,
          fontWeight: 700,
          fontSize: 15,
          textDecoration: 'none',
          boxShadow: '0 8px 32px rgba(255,111,142,0.35)',
        }}
      >
        🌸 Открыть MAX Bloom
      </a>

      <div style={{
        marginTop: 24,
        fontSize: 12,
        color: '#4A6960',
        lineHeight: 1.6,
        maxWidth: 280,
      }}>
        В боте: «Мои подписки» → «Прислать ссылку ещё раз»
      </div>
    </div>
  );
}
