import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import {
  Database,
  Search,
  FileText,
  Box,
  BookOpen,
  Loader2,
  RefreshCw,
} from "lucide-react"
import api from "@/lib/api"

export default function KnowledgeBase() {
  const [products, setProducts] = useState<any[]>([])
  const [documents, setDocuments] = useState<any[]>([])
  const [search, setSearch] = useState("")
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({ productCount: 0, docCount: 0, enabled: false })

  useEffect(() => { loadData() }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [statusRes, prodRes, docRes] = await Promise.allSettled([
        api.get("/agent/knowledge/status"),
        api.get("/agent/knowledge/products", { params: { size: 100 } }),
        api.get("/agent/knowledge/documents"),
      ])

      if (statusRes.status === "fulfilled") {
        const s = statusRes.value.data
        setStats({
          productCount: s.productCount ?? 0,
          docCount: s.knowledgeDocumentCount ?? 0,
          enabled: s.enabled ?? false,
        })
      }
      if (prodRes.status === "fulfilled") {
        const data = prodRes.value.data
        setProducts(Array.isArray(data?.items) ? data.items : Array.isArray(data) ? data : [])
      }
      if (docRes.status === "fulfilled") {
        const data = docRes.value.data
        setDocuments(Array.isArray(data?.documents) ? data.documents : Array.isArray(data) ? data : [])
      }
    } catch {}
    setLoading(false)
  }

  const filteredProducts = products.filter((p) =>
    (p.name || "").toLowerCase().includes(search.toLowerCase()) ||
    (p.sku || "").toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">RAG 知识库</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {loading ? "加载中..." : `${stats.productCount} 款产品 · ${stats.docCount} 篇文档`}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={loadData} disabled={loading}>
            <RefreshCw size={14} className={loading ? "animate-spin" : ""} />
            刷新
          </Button>
          <Badge variant={stats.enabled ? "success" : "secondary"} className="text-[11px]">
            <Database size={11} /> {stats.enabled ? "RAG Engine ON" : "RAG OFF"}
          </Badge>
        </div>
      </div>

      {/* Stats (from /status endpoint — real DB counts) */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="text-center p-4 rounded-[16px] bg-card border border-border">
          <Box size={20} className="mx-auto mb-1 text-primary" />
          <p className="text-xl font-bold">{loading ? "—" : stats.productCount}</p>
          <p className="text-[11px] text-muted-foreground">产品数据 (MySQL)</p>
        </div>
        <div className="text-center p-4 rounded-[16px] bg-card border border-border">
          <FileText size={20} className="mx-auto mb-1 text-blue-500" />
          <p className="text-xl font-bold">{loading ? "—" : stats.docCount}</p>
          <p className="text-[11px] text-muted-foreground">知识文档 (MySQL)</p>
        </div>
        <div className="text-center p-4 rounded-[16px] bg-card border border-border">
          <BookOpen size={20} className="mx-auto mb-1 text-emerald-500" />
          <p className="text-xl font-bold">{stats.enabled ? "ON" : "OFF"}</p>
          <p className="text-[11px] text-muted-foreground">RAG 引擎状态</p>
        </div>
        <div className="text-center p-4 rounded-[16px] bg-card border border-border">
          <Database size={20} className="mx-auto mb-1 text-violet-500" />
          <p className="text-xl font-bold">MySQL</p>
          <p className="text-[11px] text-muted-foreground">数据存储</p>
        </div>
      </div>

      {/* Products */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base flex items-center gap-2">
              <Box size={16} className="text-primary" /> 产品数据
              <span className="text-xs text-muted-foreground font-normal">
                (显示最近 {products.length} 条，共 {stats.productCount} 条)
              </span>
            </CardTitle>
            <div className="relative w-[200px]">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
              <Input
                placeholder="搜索产品..."
                className="pl-8 h-8 text-xs"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 size={24} className="animate-spin text-muted-foreground" />
            </div>
          ) : products.length > 0 ? (
            <div className="max-h-[360px] overflow-y-auto px-4 pb-4 space-y-1">
              {filteredProducts.map((p, i) => (
                <div
                  key={p.id || p.sku || i}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-[10px] hover:bg-muted/50 transition-colors"
                >
                  <div className="w-8 h-8 rounded-[8px] bg-muted flex items-center justify-center flex-shrink-0 overflow-hidden">
                    {p.imageUrl ? (
                      <img src={p.imageUrl} alt="" className="w-full h-full object-cover" loading="lazy" />
                    ) : (
                      <Box size={14} className="text-muted-foreground" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{p.name || `Product #${i + 1}`}</p>
                    <div className="flex items-center gap-2">
                      {p.sku && <span className="text-[10px] text-muted-foreground">SKU: {p.sku}</span>}
                      {p.category && (
                        <Badge variant="secondary" className="text-[9px] px-1.5 py-0 h-4 font-normal">{p.category}</Badge>
                      )}
                    </div>
                  </div>
                  {p.price && (
                    <span className="text-xs font-semibold text-emerald-600 flex-shrink-0">{p.price}</span>
                  )}
                </div>
              ))}
              {filteredProducts.length === 0 && (
                <p className="text-center py-8 text-muted-foreground text-sm">没有找到匹配的产品</p>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center py-12 text-muted-foreground">
              <Box size={40} className="mb-3 opacity-20" />
              <p className="text-sm">暂无产品数据</p>
              <p className="text-xs mt-1">使用 ProductScraper 爬取官网后数据将显示在这里</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Documents */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-base flex items-center gap-2">
            <FileText size={16} className="text-blue-500" /> 知识文档
            <span className="text-xs text-muted-foreground font-normal">(共 {stats.docCount} 篇)</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 size={20} className="animate-spin text-muted-foreground" />
            </div>
          ) : documents.length > 0 ? (
            <div className="space-y-2 max-h-[300px] overflow-y-auto">
              {documents.map((d, i) => (
                <div
                  key={d.id || i}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-[10px] hover:bg-muted/50 transition-colors"
                >
                  <div className="w-8 h-8 rounded-[8px] bg-blue-50 flex items-center justify-center flex-shrink-0">
                    <FileText size={14} className="text-blue-500" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{d.title || `Document #${i + 1}`}</p>
                    <div className="flex items-center gap-2">
                      {d.category && (
                        <Badge variant="secondary" className="text-[9px] px-1.5 py-0 h-4 font-normal">{d.category}</Badge>
                      )}
                      <span className="text-[10px] text-muted-foreground">{d.length ? `${d.length} 字符` : ""}</span>
                    </div>
                  </div>
                  {d.enabled !== undefined && (
                    <Badge variant={d.enabled ? "success" : "secondary"} className="text-[9px]">
                      {d.enabled ? "启用" : "禁用"}
                    </Badge>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center py-8 text-muted-foreground">
              <FileText size={32} className="mb-2 opacity-20" />
              <p className="text-sm">暂无知识文档</p>
              <p className="text-xs mt-1">将公司信息、出口指南等上传到 knowledge_documents 表</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
