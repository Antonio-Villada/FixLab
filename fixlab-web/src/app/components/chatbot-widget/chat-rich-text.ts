/** Trozo de mensaje del chat: texto plano, negrita Markdown o enlace interno [label](/ruta). */
export type ChatRichPart =
  | { kind: 'plain'; text: string }
  | { kind: 'bold'; text: string }
  | { kind: 'link'; label: string; href: string };

const LINK_RE = /\[([^\]]+)]\(([^)]+)\)/g;

function isSafeInternalPath(href: string): boolean {
  const h = href.trim();
  if (!h.startsWith('/') || h.startsWith('//')) {
    return false;
  }
  return !/^[a-zA-Z][a-zA-Z+\-.]*:/.test(h);
}

function splitBold(segment: string): ChatRichPart[] {
  if (!segment) {
    return [];
  }
  const out: ChatRichPart[] = [];
  let i = 0;
  const re = /\*\*([\s\S]+?)\*\*/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(segment)) !== null) {
    if (m.index > i) {
      out.push({ kind: 'plain', text: segment.slice(i, m.index) });
    }
    out.push({ kind: 'bold', text: m[1] });
    i = m.index + m[0].length;
  }
  if (i < segment.length) {
    out.push({ kind: 'plain', text: segment.slice(i) });
  }
  if (out.length === 0) {
    out.push({ kind: 'plain', text: segment });
  }
  return out;
}

/** Convierte texto con enlaces Markdown y **negrita** en partes renderizables. */
export function parseChatRichText(input: string): ChatRichPart[] {
  if (!input) {
    return [];
  }
  const parts: ChatRichPart[] = [];
  let i = 0;
  let m: RegExpExecArray | null;
  const re = new RegExp(LINK_RE.source, 'g');
  while ((m = re.exec(input)) !== null) {
    if (m.index > i) {
      parts.push(...splitBold(input.slice(i, m.index)));
    }
    const href = m[2].trim();
    const label = m[1];
    if (isSafeInternalPath(href)) {
      parts.push({ kind: 'link', label, href });
    } else {
      parts.push({ kind: 'plain', text: m[0] });
    }
    i = m.index + m[0].length;
  }
  if (i < input.length) {
    parts.push(...splitBold(input.slice(i)));
  }
  return parts;
}
