import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Cog, Sparkles } from "lucide-react"

export default function JCClaw() {
  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">JC-CLAW 助手</h1>
          <p className="mt-1 text-sm text-muted-foreground">智能自动化代理 · 跨系统任务编排 · 低代码工作流</p>
        </div>
        <Badge variant="success" className="text-[11px]">
          <Cog size={11} /> CLAW
        </Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm flex items-center gap-2">
              <Sparkles size={14} className="text-amber-500" />
              任务编排
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-muted-foreground">拖拽式编排多个 AI Agent，构建复杂自动化流水线。</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm flex items-center gap-2">
              <Sparkles size={14} className="text-amber-500" />
              跨系统连接
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-muted-foreground">连接 CRM、ERP、邮件系统，实现端到端业务流程自动化。</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm flex items-center gap-2">
              <Sparkles size={14} className="text-amber-500" />
              智能调度
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-muted-foreground">基于规则与 AI 决策，自动分配任务到最优执行节点。</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">任务控制台</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
            <Cog size={40} className="mb-3 opacity-20" />
            <p className="text-sm">CLAW 自动化引擎就绪</p>
            <p className="text-xs mt-1">配置工作流后在此处查看实时执行状态</p>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
