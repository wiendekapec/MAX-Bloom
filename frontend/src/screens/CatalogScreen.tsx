import { useState } from 'react';
import { useNav } from '../contexts/NavContext';
import { DEMO_COMMUNITIES, type Community, type CommunityCategory } from '../lib/api';
import { CATEGORY_LABELS, CATEGORY_GRADIENTS, CATEGORY_EMOJIS, formatRub } from '../lib/helpers';
import { hapticLight } from '../lib/maxBridge';
import { Badge } from '../components/ui';

const ALL_CATEGORIES: CommunityCategory[] = ['tech', 'education', 'fitness', 'services', 'business'];

export default function CatalogScreen() {
  const { navigate } = useNav();
  const [activeCategory, setActiveCategory] = useState<CommunityCategory | null>(null);

  const filtered = activeCategory
    ? DEMO_COMMUNITIES.filter((c) => c.category === activeCategory)
    : DEMO_COMMUNITIES;

  const handleCommunityTap = (c: Community) => {
    hapticLight();
    navigate('community', { communityId: c.id, community: c });
  };

  const handleCategoryTap = (cat: CommunityCategory) => {
    hapticLight();
    setActiveCategory((prev) => (prev === cat ? null : cat));
  };

  return (
    <div className="screen fade-in">
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          padding: '12px 0 8px',
          fontSize: 11.5,
          color: 'var(--muted)',
        }}
      >
        <Badge variant="demo">🧪 Демо</Badge>
        <span>Примеры — для иллюстрации возможностей</span>
      </div>

      <div style={{ display: 'flex', gap: 8, overflowX: 'auto', paddingBottom: 12, paddingTop: 4 }}>
        <div
          className={`chip ${activeCategory === null ? 'active' : ''}`}
          onClick={() => { hapticLight(); setActiveCategory(null); }}
          style={{ whiteSpace: 'nowrap' }}
        >
          Все
        </div>
        {ALL_CATEGORIES.map((cat) => (
          <div
            key={cat}
            className={`chip ${activeCategory === cat ? 'active' : ''}`}
            onClick={() => handleCategoryTap(cat)}
            style={{ whiteSpace: 'nowrap' }}
          >
            {CATEGORY_EMOJIS[cat]} {CATEGORY_LABELS[cat].split(' ')[1]}
          </div>
        ))}
      </div>

      {filtered.map((c) => (
        <div
          key={c.id}
          className="community-card"
          id={`catalog-community-${c.id}`}
          onClick={() => handleCommunityTap(c)}
        >
          <div
            className="community-banner"
            style={{ background: CATEGORY_GRADIENTS[c.category] }}
          >
            <div className="hero-overlay" />
            <span className="hero-emoji">{CATEGORY_EMOJIS[c.category]}</span>
          </div>

          <div className="community-body">
            <div className="community-title">{c.title}</div>
            <div className="community-desc" style={{ WebkitLineClamp: 2, display: '-webkit-box', WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
              {c.description}
            </div>

            <div className="community-footer">
              <div className="community-subs">
                <span>👥</span>
                <span>{c.subscribersCount ?? 0} подписчиков</span>
              </div>
              <div className={`category-badge`}>
                {CATEGORY_LABELS[c.category]}
              </div>
            </div>

            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 10 }}>
              {c.plans.slice(0, 2).map((p) => (
                <span
                  key={p.id}
                  style={{
                    fontSize: 12,
                    fontWeight: 600,
                    padding: '4px 10px',
                    borderRadius: 'var(--radius-full)',
                    background: 'rgba(255,111,142,0.12)',
                    color: 'var(--bloom)',
                    border: '1px solid rgba(255,111,142,0.2)',
                  }}
                >
                  от {formatRub(p.priceRub)}
                </span>
              ))}
            </div>
          </div>
        </div>
      ))}

      <div style={{ height: 20 }} />
    </div>
  );
}
