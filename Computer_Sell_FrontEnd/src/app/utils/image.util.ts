export function buildImageUrl(baseUrl: string, path?: string): string | null {
  if (!path) return null;
  if (/^https?:\/\//i.test(path)) return path;
  let p = path.startsWith('/') ? path : `/${path}`;
  if (!p.startsWith('/uploads/')) {
    p = `/uploads/products${p}`;
  }
  return `${baseUrl}${p}`;
}

