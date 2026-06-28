<template>
  <section class="rounded-lg border border-blue-100 bg-white p-5 shadow-sm shadow-blue-100/50" aria-labelledby="agent-flow-title">
    <div class="mb-5 flex items-center justify-between gap-3">
      <div>
        <h2 id="agent-flow-title" class="text-sm font-semibold text-blue-950">智能体流程</h2>
        <p class="mt-1 text-xs text-slate-500">研究任务的节点执行进度</p>
      </div>
      <span class="rounded-md border border-blue-100 bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700">
        {{ statusLabel }}
      </span>
    </div>

    <ol class="relative space-y-4">
      <li
        v-for="(step, index) in steps"
        :key="step.id"
        class="relative flex gap-3"
      >
        <div
          v-if="index !== steps.length - 1"
          class="absolute left-4 top-8 h-[calc(100%+0.25rem)] w-px bg-slate-200"
          aria-hidden="true"
        ></div>

        <div
          class="relative z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border transition"
          :class="getMarkerStyles(step, index)"
          :aria-label="`${step.label} ${getStepState(step, index)}`"
        >
          <Loader2Icon v-if="isActive(step)" class="h-4 w-4 animate-spin" aria-hidden="true" />
          <CheckIcon v-else-if="isCompleted(step, index)" class="h-4 w-4" aria-hidden="true" />
          <component v-else :is="step.icon" class="h-4 w-4" aria-hidden="true" />
        </div>

        <div class="min-w-0 flex-1 pb-1">
          <div class="flex items-center justify-between gap-3">
            <h3 class="truncate text-sm font-semibold" :class="getTitleStyles(step, index)">
              {{ step.label }}
            </h3>
            <span class="shrink-0 text-[11px] font-medium uppercase tracking-wide" :class="getStateStyles(step, index)">
              {{ getStepState(step, index) }}
            </span>
          </div>
          <p class="mt-1 text-xs leading-5 text-slate-500">{{ step.desc }}</p>
        </div>
      </li>
    </ol>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import {
  BrainCircuitIcon,
  CheckIcon,
  FilePenLineIcon,
  FileTextIcon,
  Loader2Icon,
  SearchIcon,
  ShieldCheckIcon
} from 'lucide-vue-next';

const props = defineProps({
  currentStep: { type: String, default: 'idle' },
  completedSteps: { type: Array, default: () => [] }
});

const steps = [
  { id: 'planner', label: '规划', desc: '拆解任务并制定检索计划', icon: BrainCircuitIcon },
  { id: 'researcher', label: '检索', desc: '收集文档证据和联网资料', icon: SearchIcon },
  { id: 'writer', label: '撰写', desc: '生成 Markdown 研究报告', icon: FileTextIcon },
  { id: 'reviewer', label: '质检', desc: '检查报告质量并决定是否重试', icon: ShieldCheckIcon },
  { id: 'refiner', label: '修订', desc: '基于同一会话继续调整报告', icon: FilePenLineIcon, optional: true }
];

const seenSteps = ref(new Set());

watch(
  () => props.currentStep,
  (step) => {
    if (step && step !== 'idle' && step !== 'done') {
      seenSteps.value = new Set([...seenSteps.value, step]);
    }
  },
  { immediate: true }
);

const currentStepIndex = computed(() => steps.findIndex((step) => step.id === props.currentStep));
const completedStepSet = computed(() => new Set([...props.completedSteps, ...seenSteps.value]));

const statusLabel = computed(() => {
  if (props.currentStep === 'idle') return '待开始';
  if (props.currentStep === 'done') return '已完成';
  return '进行中';
});

const isActive = (step) => props.currentStep === step.id;

const isCompleted = (step, index) => {
  if (!isActive(step) && completedStepSet.value.has(step.id)) return true;
  if (currentStepIndex.value > index) return true;
  return false;
};

const getStepState = (step, index) => {
  if (isActive(step)) return '进行中';
  if (isCompleted(step, index)) return '已完成';
  if (step.optional) return '可选';
  return '待开始';
};

const getMarkerStyles = (step, index) => {
  if (isActive(step)) return 'border-blue-700 bg-blue-700 text-white shadow-sm';
  if (isCompleted(step, index)) return 'border-emerald-600 bg-emerald-600 text-white';
  return 'border-blue-100 bg-blue-50/60 text-slate-400';
};

const getTitleStyles = (step, index) => {
  if (isActive(step)) return 'text-blue-800';
  if (isCompleted(step, index)) return 'text-slate-950';
  return 'text-slate-500';
};

const getStateStyles = (step, index) => {
  if (isActive(step)) return 'text-blue-700';
  if (isCompleted(step, index)) return 'text-emerald-700';
  if (step.optional) return 'text-slate-400';
  return 'text-slate-400';
};
</script>
