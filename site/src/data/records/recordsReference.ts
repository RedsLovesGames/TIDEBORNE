export type RecordEvent = {
  name: string;
  description: string;
  liveCatchEvent: boolean;
};

export const leaderboardMetrics = [
  'Catches',
  'Unique Species',
  'Records Set',
  'Active Records',
  'Best FishScore',
] as const;

export const recordEventTypes: RecordEvent[] = [
  {
    name: 'First Discovery',
    description: 'The first tracked team discovery of a fish species.',
    liveCatchEvent: true,
  },
  {
    name: 'New Largest',
    description: "A specimen beats the team's current largest record for its species.",
    liveCatchEvent: true,
  },
  {
    name: 'New Smallest',
    description: "A specimen beats the team's current smallest record for its species.",
    liveCatchEvent: true,
  },
  {
    name: 'Ownership Repair',
    description: 'Administrative recovery corrects record ownership without inventing a new catch.',
    liveCatchEvent: false,
  },
];

export const hallSortModes = ['Best', 'Worst', 'Newest', 'Oldest'] as const;

export const sharedTeamData = [
  'Discovered species',
  'Total catches',
  'Unique species',
  'Largest and smallest records',
  'Record owners',
  'Record history',
  'Contributor statistics',
  'Active records',
  'Best FishScore',
  'Team Top Fish / Top 15',
] as const;

export const journalCommands = [
  { command: '/ttj', purpose: 'Open Team Records' },
  { command: '/ttj leaderboard [metric] [page]', purpose: 'Browse a team leaderboard' },
  { command: '/ttj history [page]', purpose: 'Browse record history' },
  { command: '/ttj history fish <fish_id> [page]', purpose: 'Filter history to a species' },
  { command: '/ttj member <name-or-uuid>', purpose: 'Inspect a member contribution summary' },
  { command: '/ttj merge', purpose: 'One-time personal-progress merge' },
  { command: '/ttj status', purpose: 'Inspect held-fish record ownership' },
] as const;
