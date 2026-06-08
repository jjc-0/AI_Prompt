import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"
import { Progress } from "@/components/ui/progress"
import {
  Play,
  Pause,
  RotateCcw,
  CheckCircle2,
  Clock,
  AlertCircle,
  Loader2,
  Zap,
  MessageSquare,
  Globe,
  FileText,
  Search,
  Cog,
  ExternalLink,
} from "lucide-react"

interface LogEntry {
  id: string
  timestamp: string
  type: "info" | "success" | "error" | "warning"
  agent: string
  message: string
}

interface ToolCall {
  id: string
  tool: string
  icon: React.ReactNode
  status: "running" | "completed" | "pending" | "error"
  startTime: string
  duration?: string
  result?: string
}

const logs: LogEntry[] = [
  { id: "1", timestamp: "14:32:01", type: "info", agent: "客服 Agent", message: "收到新对话请求 #10842" },
  { id: "2", timestamp: "14:32:03", type: "info", agent: "客服 Agent", message: "调用知识库检索产品 JC-1289" },
  { id: "3", timestamp: "14:32:04", type: "success", agent: "RAG 引擎", message: "检索完成，返回 3 条匹配文档" },
  { id: "4", timestamp: "14:32:06", type: "info", agent: "翻译 Agent", message: "启动中→英产品描述翻译" },
  { id: "5", timestamp: "14:32:08", type: "warning", agent: "分析 Agent", message: "SEO 工具 API 延迟 200ms" },
  { id: "6", timestamp: "14:32:10", type: "success", agent: "翻译 Agent", message: "翻译完成，字符数: 2,847" },
  { id: "7", timestamp: "14:32:12", type: "error", agent: "爬取 Agent", message: "Alibaba 页面结构变更，解析失败" },
  { id: "8", timestamp: "14:32:15", type: "info", agent: "客服 Agent", message: "生成回复文案，置信度: 0.94" },
  { id: "9", timestamp: "14:32:17", type: "success", agent: "客服 Agent", message: "回复已发送给客户" },
]

const toolCalls: ToolCall[] = [
  {
    id: "t1", tool: "知识库检索", icon: <Search size={14} />,
    status: "completed", startTime: "14:32:03", duration: "1.2s", result: "3 docs"
  },
  {
    id: "t2", tool: "多语言翻译", icon: <Globe size={14} />,
    status: "completed", startTime: "14:32:06", duration: "2.1s", result: "2,847 chars"
  },
  {
    id: "t3", tool: "产品价格查询", icon: <Cog size={14} />,
    status: "running", startTime: "14:32:09",
  },
  {
    id: "t4", tool: "SEO 关键词分析", icon: <Search size={14} />,
    status: "completed", startTime: "14:32:04", duration: "0.8s", result: "15 keywords"
  },
  {
    id: "t5", tool: "网页内容抓取", icon: <ExternalLink size={14} />,
    status: "error", startTime: "14:32:10", result: "Parse Error"
  },
]

const activeAgents = [
  { name: "客服 Agent", task: "产品咨询回复", progress: 78, status: "running" },
  { name: "翻译 Agent", task: "批量产品翻译", progress: 45, status: "running" },
  { name: "分析 Agent", task: "竞品分析", progress: 92, status: "running" },
]

const statusConfig = {
  running: { icon: Loader2, color: "text-blue-500", bg: "bg-blue-50 text-blue-700", label: "运行中", animated: true },
  completed: { icon: CheckCircle2, color: "text-emerald-500", bg: "bg-emerald-50 text-emerald-700", label: "已完成", animated: false },
  pending: { icon: Clock, color: "text-muted-foreground", bg: "bg-muted text-muted-foreground", label: "等待中", animated: false },
  error: { icon: AlertCircle, color: "text-red-500", bg: "bg-red-50 text-red-700", label: "失败", animated: false },
}

const logTypeConfig = {
  info: "bg-slate-50 text-slate-700",
  success: "bg-emerald-50 text-emerald-700",
  error: "bg-red-50 text-red-700",
  warning: "bg-amber-50 text-amber-700",
}

export default function AgentExecutionCenter() {
  const [isRunning, setIsRunning] = useState(true)

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Agent 执行中心</h1>
          <p className="mt-1 text-sm text-muted-foreground">实时监控 Agent 任务执行、工具调用与运行日志</p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsRunning(!isRunning)}
          >
            {isRunning ? <Pause size={14} /> : <Play size={14} />}
            {isRunning ? "暂停" : "启动"}
          </Button>
          <Button variant="outline" size="sm">
            <RotateCcw size={14} />
            重置
          </Button>
        </div>
      </div>

      {/* Active Agents Status */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {activeAgents.map((agent, i) => (
          <Card key={agent.name} className={`animate-fade-in-up stagger-${i + 1}`}>
            <CardContent className="p-5">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2.5">
                  <Loader2 size={15} className="text-blue-500 animate-spin" />
                  <div>
                    <p className="text-sm font-semibold">{agent.name}</p>
                    <p className="text-[11px] text-muted-foreground">{agent.task}</p>
                  </div>
                </div>
                <Badge variant="blue" className="text-[10px]">运行中</Badge>
              </div>
              <Progress value={agent.progress} className="h-1.5" />
              <p className="text-[11px] text-muted-foreground mt-2">{agent.progress}% 完成</p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Main Content: Tools + Logs */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Tool Calls Panel */}
        <Card className="lg:col-span-1 animate-fade-in-up stagger-4">
          <CardHeader className="pb-3">
            <CardTitle className="text-base flex items-center gap-2">
              <Zap size={16} className="text-amber-500" />
              工具调用
            </CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <ScrollArea className="h-[360px]">
              <div className="px-4 space-y-1">
                {toolCalls.map((call) => {
                  const status = statusConfig[call.status]
                  const StatusIcon = status.icon
                  return (
                    <div
                      key={call.id}
                      className="flex items-center gap-3 py-2.5 px-2 rounded-[10px] hover:bg-muted/50 transition-colors"
                    >
                      <div className="w-8 h-8 rounded-[10px] bg-muted flex items-center justify-center flex-shrink-0">
                        {call.icon}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="text-sm font-medium truncate">{call.tool}</p>
                          <StatusIcon
                            size={12}
                            className={`${status.color} flex-shrink-0 ${status.animated ? "animate-spin" : ""}`}
                          />
                        </div>
                        <div className="flex items-center gap-2 mt-0.5">
                          <span className="text-[10px] text-muted-foreground">{call.startTime}</span>
                          {call.duration && (
                            <span className="text-[10px] text-muted-foreground">{call.duration}</span>
                          )}
                          {call.result && (
                            <Badge variant="secondary" className="text-[9px] px-1.5 py-0 h-4">
                              {call.result}
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>

        {/* Execution Logs */}
        <Card className="lg:col-span-2 animate-fade-in-up stagger-5">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <CardTitle className="text-base flex items-center gap-2">
                <FileText size={16} className="text-muted-foreground" />
                执行日志
              </CardTitle>
              <Badge variant="outline" className="text-[10px]">{logs.length} 条</Badge>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            <ScrollArea className="h-[360px]">
              <div className="px-4 pb-2">
                {logs.map((log, i) => (
                  <div key={log.id}>
                    {i > 0 && <Separator className="my-1 ml-10" />}
                    <div className="flex items-start gap-3 py-2 px-2 rounded-[8px] hover:bg-muted/30 transition-colors">
                      <div className={`w-7 h-7 rounded-[10px] flex items-center justify-center flex-shrink-0 ${logTypeConfig[log.type]}`}>
                        {log.type === "error" ? (
                          <AlertCircle size={13} />
                        ) : log.type === "warning" ? (
                          <AlertCircle size={13} />
                        ) : log.type === "success" ? (
                          <CheckCircle2 size={13} />
                        ) : (
                          <span className="w-1.5 h-1.5 rounded-full bg-current" />
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <Badge variant="secondary" className="text-[10px] px-1.5 py-0 h-4 font-normal">
                            {log.agent}
                          </Badge>
                          <span className="text-[10px] text-muted-foreground">{log.timestamp}</span>
                        </div>
                        <p className="text-[13px] mt-0.5 leading-relaxed">{log.message}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 animate-fade-in-up">
        {[
          { label: "活跃 Agent", value: "3", color: "text-blue-600" },
          { label: "工具调用", value: "18", color: "text-amber-600" },
          { label: "成功率", value: "97.2%", color: "text-emerald-600" },
          { label: "平均延迟", value: "1.4s", color: "text-violet-600" },
        ].map((stat) => (
          <div key={stat.label} className="text-center p-3 rounded-[16px] bg-card border border-border">
            <p className={`text-xl font-bold ${stat.color}`}>{stat.value}</p>
            <p className="text-[11px] text-muted-foreground mt-0.5">{stat.label}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
