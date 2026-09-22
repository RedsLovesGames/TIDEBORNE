export type RoutableFish = {
  id: string;
  namespace?: string | null;
  slug?: string | null;
};

const routeKey = (record: RoutableFish) => `${record.namespace ?? ''}/${record.slug ?? ''}`;

export function buildFishRoutes<T extends RoutableFish>(records: readonly T[]) {
  const seen = new Set<string>();

  return [...records]
    .filter((record) => Boolean(record.namespace && record.slug))
    .sort((a, b) => routeKey(a).localeCompare(routeKey(b)))
    .map((record) => {
      const key = routeKey(record);
      if (seen.has(key)) {
        throw new Error(`Duplicate fish detail route: ${key}`);
      }
      seen.add(key);
      return {
        params: { namespace: record.namespace as string, slug: record.slug as string },
        props: { record },
      };
    });
}

export function findFishByRoute<T extends RoutableFish>(
  records: readonly T[],
  namespace: string,
  slug: string,
): T | undefined {
  return records.find((record) => record.namespace === namespace && record.slug === slug);
}
