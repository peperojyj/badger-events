const VISUALS = [
  {
    terms: ['career', 'advising', 'business'],
    icon: '💼',
    label: 'Career',
    tone: 'gold',
  },
  {
    terms: ['art', 'design', 'film', 'theatre'],
    icon: '🎨',
    label: 'Arts',
    tone: 'plum',
  },
  {
    terms: ['music', 'concert', 'dance'],
    icon: '♪',
    label: 'Music',
    tone: 'blue',
  },
  {
    terms: ['sport', 'athletic', 'fitness'],
    icon: '●',
    label: 'Sports',
    tone: 'green',
  },
  {
    terms: [
      'science',
      'research',
      'engineering',
      'stem',
    ],
    icon: '⌬',
    label: 'Science',
    tone: 'teal',
  },
  {
    terms: [
      'health',
      'wellness',
      'nature',
      'environment',
    ],
    icon: '✦',
    label: 'Wellness',
    tone: 'sage',
  },
  {
    terms: ['lecture', 'academic', 'learning'],
    icon: '▤',
    label: 'Academic',
    tone: 'navy',
  },
]

const DEFAULT_VISUAL = {
  icon: '◆',
  label: 'Campus event',
  tone: 'red',
}

export function categoryVisual(category) {
  const normalizedCategory =
    (category || '').toLowerCase()

  return (
    VISUALS.find(({ terms }) =>
      terms.some((term) =>
        normalizedCategory.includes(term),
      ),
    ) || DEFAULT_VISUAL
  )
}