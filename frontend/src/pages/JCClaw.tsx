import { useState, useRef, useEffect } from "react"
import { QRCodeSVG } from "qrcode.react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  MessageCircle,
  Bot,
  QrCode,
  Users,
  MessageSquareOff,
  Check,
  RefreshCw,
  Sparkles,
} from "lucide-react"
import { cn } from "@/lib/utils"
import api from "@/lib/api"

export default function JCClaw() {
  const [qrUrl, setQrUrl] = useState<string | null>(null)
  const [isBinding, setIsBinding] = useState(false)
  const [isBound, setIsBound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const [apiOnline, setApiOnline] = useState(true)

  useEffect(() => {
    return () => { if (pollingRef.current) clearInterval(pollingRef.current) }
  }, [])

  /** 发起真实扫码绑定 */
  const startBind = async () => {
    setError(null)
    setIsBinding(true)
    try {
      const res = await api.post("/claw/bind/start")
      if (res.data?.success && res.data.qrUrl) {
        setQrUrl(res.data.qrUrl)
        startPolling()
      } else {
        setError(res.data?.error || "获取二维码失败")
        setIsBinding(false)
      }
    } catch (e) {
      setError("后端服务未启动，请先运行 Spring Boot")
      setIsBinding(false)
      setApiOnline(false)
    }
  }

  /** 轮询 OpenClaw 连接状态 */
  const startPolling = () => {
    if (pollingRef.current) clearInterval(pollingRef.current)
    pollingRef.current = setInterval(async () => {
      try {
        const res = await api.get("/claw/bind/status")
        if (res.data?.connected) {
          clearInterval(pollingRef.current!)
          pollingRef.current = null
          setQrUrl(null)
          setIsBinding(false)
          setIsBound(true)
        }
      } catch { /* ignore */ }
    }, 2000)
  }

  /** 取消绑定 */
  const cancelBind = () => {
    if (pollingRef.current) { clearInterval(pollingRef.current); pollingRef.current = null }
    api.post("/claw/bind/cancel").catch(() => {})
    setIsBinding(false)
    setQrUrl(null)
  }

  /** 解绑 */
  const unbind = () => setIsBound(false)

  return (
    <div className="space-y-5 animate-fade-in">
      {/* ── 标题 ── */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Sparkles size={14} className="text-emerald-500" />
            <span className="text-[11px] font-medium text-muted-foreground tracking-wide uppercase">JC-CLAW 助手</span>
          </div>
          <h1 className="mt-1 text-xl font-bold tracking-tight text-foreground">连接每一个触点，让 AI 无界触达</h1>
          <p className="mt-1 text-[13px] text-muted-foreground max-w-lg">通过 OpenClaw 扫码绑定微信，将智能体注入社交渠道，实现自动化消息处理与智能回复。</p>
        </div>
        {!apiOnline && (
          <Badge variant="warning" className="text-[10px]">后端离线</Badge>
        )}
      </div>

      {/* ── 微信绑定卡片 ── */}
      <Card className="w-[360px]">
        <CardContent className="p-4 space-y-3">

          {/* 头部 */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-[10px] bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center text-white shadow">
                <MessageCircle size={16} />
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <span className="text-[13px] font-bold">微信ClawBot</span>
                  <Badge variant={isBound ? "success" : isBinding ? "warning" : "secondary"} className="text-[10px] px-1.5 py-0">
                    {isBound ? <><Check size={9} />已关联</> : isBinding ? "绑定中" : "未关联"}
                  </Badge>
                </div>
                <p className="text-[11px] text-muted-foreground">OpenClaw 微信扫码绑定</p>
              </div>
            </div>
          </div>

          {/* 用户数 / 群聊 */}
          <div className="flex gap-2">
            <div className="flex-1 flex items-center gap-2 p-2.5 rounded-[10px] bg-blue-50/60 border border-blue-100/60">
              <div className="w-7 h-7 rounded-[8px] bg-gradient-to-br from-blue-500 to-indigo-500 flex items-center justify-center text-white shadow-sm">
                <Users size={14} />
              </div>
              <div>
                <p className="text-[10px] text-muted-foreground leading-none">用户</p>
                <p className="text-[22px] font-bold leading-none mt-0.5">{isBound ? 1 : 0}</p>
              </div>
            </div>
            <div className="flex-1 flex items-center gap-2 p-2.5 rounded-[10px] bg-slate-50/70 border border-slate-200/60">
              <MessageSquareOff size={14} className="text-slate-400" />
              <p className="text-[11px] text-muted-foreground font-medium">暂不支持群聊</p>
            </div>
          </div>

          {/* 智能体 */}
          <div className={cn("w-full flex items-center gap-2.5 p-2.5 rounded-[10px] border", "border-primary bg-primary/5 ring-1 ring-primary/15")}>
            <div className="w-7 h-7 rounded-[8px] bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white shadow-sm">
              <Bot size={14} />
            </div>
            <span className="text-[12px] font-semibold flex-1">JC-claw</span>
            <Check size={12} className="text-primary" />
          </div>

          {/* 错误提示 */}
          {error && (
            <div className="p-2.5 rounded-[10px] bg-red-50 border border-red-200 text-[11px] text-red-600">{error}</div>
          )}

          {/* 二维码区域 */}
          {isBinding && qrUrl && (
            <div className="flex flex-col items-center gap-2 p-3 rounded-[10px] bg-white border border-border animate-fade-in">
              <QRCodeSVG value={qrUrl} size={140} level="M" fgColor="#1A1D2E" bgColor="#FFFFFF" includeMargin />
              <div className="flex items-center gap-1.5 text-[10px] text-muted-foreground">
                <RefreshCw size={10} className="animate-spin" />
                请用手机微信扫描二维码
              </div>
            </div>
          )}

          {/* 操作按钮 */}
          {isBound ? (
            <Button size="sm" variant="destructive" className="w-full h-8 text-[11px]" onClick={unbind}>
              解绑微信账号
            </Button>
          ) : isBinding ? (
            <Button size="sm" variant="secondary" className="w-full h-8 text-[11px]" onClick={cancelBind}>
              取消绑定
            </Button>
          ) : (
            <Button size="sm" variant="outline" className="w-full h-8 text-[11px]" onClick={startBind}>
              <QrCode size={13} />
              扫码绑定
            </Button>
          )}

          <Button
            size="sm"
            className="w-full h-9 rounded-[10px] text-[12px] font-semibold bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 shadow active:scale-[0.98] transition-all"
            disabled={!isBound}
          >
            <MessageCircle size={14} />
            {isBound ? "已绑定微信账号" : "请先完成扫码绑定"}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
