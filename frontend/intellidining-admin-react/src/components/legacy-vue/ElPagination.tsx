import { useEffect, useMemo, useRef, useState } from 'react'

export function ElPagination({
  className,
  page,
  pageSize,
  total,
  pageSizes = [10, 20, 30, 40],
  onPageChange,
  onPageSizeChange,
}: {
  className?: string
  page: number
  pageSize: number
  total: number
  pageSizes?: number[]
  onPageChange: (page: number) => void
  onPageSizeChange: (pageSize: number) => void
}) {
  const pageCount = Math.max(1, Math.ceil(total / pageSize))
  const safePage = Math.min(Math.max(1, page), pageCount)

  const [sizesOpen, setSizesOpen] = useState(false)
  const [jump, setJump] = useState(String(safePage))
  const rootRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    setJump(String(safePage))
  }, [safePage])

  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (!sizesOpen) return
      const el = rootRef.current
      if (!el) return
      if (e.target instanceof Node && el.contains(e.target)) return
      setSizesOpen(false)
    }
    document.addEventListener('click', onDocClick)
    return () => document.removeEventListener('click', onDocClick)
  }, [sizesOpen])

  const pagerNumbers = useMemo(() => {
    const max = 7
    if (pageCount <= max) return Array.from({ length: pageCount }, (_, i) => i + 1)
    const res: number[] = []
    const start = Math.max(1, safePage - 2)
    const end = Math.min(pageCount, safePage + 2)
    res.push(1)
    for (let i = start; i <= end; i++) if (i !== 1 && i !== pageCount) res.push(i)
    if (pageCount !== 1) res.push(pageCount)
    return Array.from(new Set(res)).sort((a, b) => a - b)
  }, [pageCount, safePage])

  return (
    <div ref={rootRef} className={['el-pagination', 'is-background', className || ''].filter(Boolean).join(' ')}>
      <span className="el-pagination__total">共 {total} 条</span>

      <span className="el-pagination__sizes">
        <div className="el-select">
          <div className="el-input el-input--suffix" onClick={() => setSizesOpen((v) => !v)} role="button" tabIndex={0}>
            <input className="el-input__inner" readOnly value={`${pageSize}条/页`} />
            <span className="el-input__suffix">
              <span className="el-input__suffix-inner">
                <i className="el-select__caret el-input__icon el-icon-arrow-up" />
              </span>
            </span>
          </div>
          {sizesOpen ? (
            <div className="el-select-dropdown el-popper" style={{ minWidth: 110 }}>
              <div className="el-scrollbar">
                <div className="el-select-dropdown__wrap el-scrollbar__wrap">
                  <ul className="el-scrollbar__view el-select-dropdown__list">
                    {pageSizes.map((ps) => (
                      <li
                        key={ps}
                        className={[
                          'el-select-dropdown__item',
                          ps === pageSize ? 'selected' : '',
                        ]
                          .filter(Boolean)
                          .join(' ')}
                        onClick={() => {
                          setSizesOpen(false)
                          onPageSizeChange(ps)
                          onPageChange(1)
                        }}
                      >
                        <span>{ps}条/页</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </div>
          ) : null}
        </div>
      </span>

      <button
        type="button"
        className="btn-prev"
        disabled={safePage <= 1}
        onClick={() => onPageChange(Math.max(1, safePage - 1))}
      >
        <i className="el-icon el-icon-arrow-left" />
      </button>

      <ul className="el-pager">
        {pagerNumbers.map((n) => (
          <li
            key={n}
            className={['number', n === safePage ? 'active' : ''].filter(Boolean).join(' ')}
            onClick={() => onPageChange(n)}
          >
            {n}
          </li>
        ))}
      </ul>

      <button
        type="button"
        className="btn-next"
        disabled={safePage >= pageCount}
        onClick={() => onPageChange(Math.min(pageCount, safePage + 1))}
      >
        <i className="el-icon el-icon-arrow-right" />
      </button>

      <span className="el-pagination__jump">
        前往
        <div className="el-input el-pagination__editor is-in-pagination">
          <input
            className="el-input__inner"
            value={jump}
            onChange={(e) => setJump(e.target.value)}
            onKeyDown={(e) => {
              if (e.key !== 'Enter') return
              const v = Number(jump)
              if (!Number.isFinite(v)) return
              onPageChange(Math.min(pageCount, Math.max(1, Math.floor(v))))
            }}
          />
        </div>
        页
      </span>
    </div>
  )
}

