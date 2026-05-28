<template>
  <div
    class="bingo-card"
    :class="{ 'card-highlighted': highlighted }"
    :aria-label="`Cartela ${cardIndex + 1}`"
  >
    <!-- Header of the Card: B I N G O -->
    <div class="card-header-grid">
      <div v-for="letter in ['B', 'I', 'N', 'G', 'O']" :key="letter" class="letter-cell">
        {{ letter }}
      </div>
    </div>

    <!-- Body of the Card: 5x5 Grid -->
    <div class="card-grid">
      <template v-for="row in 5" :key="row">
        <div
          v-for="(colLetter, colIdx) in ['b', 'i', 'n', 'g', 'o']"
          :key="colIdx"
          class="number-cell"
          :class="{
            'free-cell': row === 3 && colIdx === 2,
            'marked-cell': isCellMarked(row - 1, colIdx),
          }"
        >
          <div class="cell-content">
            <template v-if="row === 3 && colIdx === 2">
              <v-icon class="star-icon" size="small">mdi-star</v-icon>
              <span class="free-text">GRÁTIS</span>
            </template>
            <template v-else>
              <span class="cell-number">{{ getCellValue(row - 1, colIdx) }}</span>
            </template>
          </div>
        </div>
      </template>
    </div>

    <!-- Footer of the Card: Label & Info -->
    <div class="card-footer">
      <span class="card-label">CARTELA #{{ cardIndex + 1 }}</span>
      <span class="card-progress">
        {{ markedCount }}/24 Marcadas
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CardMatrixDto } from '../../services/api'

interface Props {
  card: CardMatrixDto
  cardIndex: number
  markedNumbers?: number[] | Set<number>
  highlighted?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  markedNumbers: () => new Set<number>(),
  highlighted: false
})

// Check if a cell is marked
function isCellMarked(rowIndex: number, colIndex: number): boolean {
  if (rowIndex === 2 && colIndex === 2) return true // FREE space is always marked!
  
  const value = getCellValue(rowIndex, colIndex)
  if (typeof value !== 'number') return false
  
  if (props.markedNumbers instanceof Set) {
    return props.markedNumbers.has(value)
  } else if (Array.isArray(props.markedNumbers)) {
    return props.markedNumbers.includes(value)
  }
  return false
}

// Get the cell value from column-first storage
function getCellValue(rowIndex: number, colIndex: number): number | string {
  const columnKeys: (keyof CardMatrixDto)[] = ['b', 'i', 'n', 'g', 'o']
  const columnKey = columnKeys[colIndex]
  const col = props.card[columnKey]
  
  if (!col) return ''
  const val = col[rowIndex]
  
  if (rowIndex === 2 && colIndex === 2) return 'FREE'
  return val !== undefined ? val : ''
}

// Calculate the number of marked cells (excluding the FREE center which is already marked)
const markedCount = computed(() => {
  let count = 0
  for (let r = 0; r < 5; r++) {
    for (let c = 0; c < 5; c++) {
      if (r === 2 && c === 2) continue
      if (isCellMarked(r, c)) {
        count++
      }
    }
  }
  return count
})
</script>

<style scoped>
.bingo-card {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(224, 224, 224, 0.8);
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  flex-direction: column;
  gap: 12px;
  user-select: none;
}

.bingo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(26, 35, 126, 0.12);
  border-color: rgba(26, 35, 126, 0.3);
}

.card-highlighted {
  border-color: #FFD600 !important;
  box-shadow: 0 0 20px rgba(255, 214, 0, 0.35) !important;
}

/* Header of the Card (B I N G O) */
.card-header-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  text-align: center;
}

.letter-cell {
  background: linear-gradient(135deg, #1a237e 0%, #0d47a1 100%);
  color: #ffffff;
  font-weight: 900;
  font-size: 1.35rem;
  padding: 8px 0;
  border-radius: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.05em;
  box-shadow: 0 4px 10px rgba(13, 71, 161, 0.2);
}

/* 5x5 Number Grid */
.card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  aspect-ratio: 1;
}

.number-cell {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
}

.number-cell:hover:not(.free-cell) {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.cell-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.cell-number {
  font-size: 1.25rem;
  font-weight: 800;
  color: #1e293b;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* Marked Cell Styles */
.marked-cell {
  border-color: rgba(46, 125, 50, 0.2);
}

.marked-cell .cell-content::before {
  content: '';
  position: absolute;
  width: 80%;
  height: 80%;
  background: radial-gradient(circle, #4caf50 0%, #2e7d32 100%);
  border-radius: 50%;
  z-index: 1;
  animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
  box-shadow: 0 4px 8px rgba(46, 125, 50, 0.3);
}

.marked-cell .cell-number {
  color: #ffffff;
  position: relative;
  z-index: 2;
  font-size: 1.35rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* FREE/GRÁTIS Cell Styles */
.free-cell {
  background: radial-gradient(circle, #fff9c4 0%, #ffd54f 100%);
  border-color: #ffca28;
  display: flex;
  flex-direction: column;
  color: #b78103;
}

.free-cell .star-icon {
  color: #ff8f00 !important;
  font-size: 1.2rem;
  animation: spin-pulse 3s linear infinite;
}

.free-text {
  font-size: 0.55rem;
  font-weight: 950;
  letter-spacing: 0.05em;
  margin-top: 1px;
}

/* Card Footer */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 6px 0;
  border-top: 1px dashed rgba(224, 224, 224, 0.6);
  margin-top: 4px;
}

.card-label {
  font-size: 0.8rem;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.05em;
}

.card-progress {
  font-size: 0.75rem;
  font-weight: 600;
  color: #2e7d32;
  background: rgba(46, 125, 50, 0.08);
  padding: 2px 8px;
  border-radius: 12px;
}

/* Keyframes animations */
@keyframes pop-in {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes spin-pulse {
  0% {
    transform: scale(1) rotate(0deg);
  }
  50% {
    transform: scale(1.15) rotate(180deg);
  }
  100% {
    transform: scale(1) rotate(360deg);
  }
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .bingo-card {
    padding: 10px;
    gap: 8px;
  }
  .letter-cell {
    font-size: 1rem;
    padding: 4px 0;
    border-radius: 6px;
  }
  .card-grid {
    gap: 4px;
  }
  .number-cell {
    border-radius: 8px;
  }
  .cell-number {
    font-size: 0.95rem;
  }
  .marked-cell .cell-number {
    font-size: 1.05rem;
  }
  .free-cell .star-icon {
    font-size: 0.9rem;
  }
  .free-text {
    font-size: 0.45rem;
  }
  .card-label {
    font-size: 0.7rem;
  }
  .card-progress {
    font-size: 0.65rem;
    padding: 1px 6px;
  }
}
</style>
