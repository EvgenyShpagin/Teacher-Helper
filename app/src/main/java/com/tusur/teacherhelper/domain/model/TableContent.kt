package com.tusur.teacherhelper.domain.model

class TableContent<Item>(val columnCount: Int) {

    constructor(
        columnCount: Int,
        rowCount: Int,
        initializer: (index: Int) -> List<Item>
    ) : this(columnCount) {
        (0 until rowCount).forEach { i ->
            val currentRow = initializer(i)
            assert(columnCount == currentRow.count()) {
                "Table row $i has different item count (required : $columnCount, found: ${currentRow.count()})"
            }
            addRow(currentRow)
        }
    }

    constructor(
        labels: List<Item>,
        rowCount: Int,
        initializer: (index: Int) -> List<Item>
    ) : this(labels.count(), rowCount, initializer) {
        this.columnLabels = labels
    }

    init {
        assert(columnCount >= 0)
    }

    var columnLabels = emptyList<Item>()
        set(value) {
            assert(value.count() == columnCount) {
                "Table labels must be the same size as column count (got ${value.count()} labels, column count: $columnCount)"
            }
            field = value
        }

    val rowCount get() = rows.count()

    private var rows = ArrayList<List<Item>>()

    fun forEach(action: (Item, row: Int, col: Int) -> Unit) {
        rows.forEachIndexed { rowIndex, items ->
            items.forEachIndexed { columnIndex, item ->
                action.invoke(item, rowIndex, columnIndex)
            }
        }
    }

    fun isEmpty(): Boolean {
        return columnCount == 0
    }

    fun editContent(
        action: (rowIndex: Int, columnIndex: Int, value: Item) -> Item
    ): TableContent<Item> {
        return TableContent(columnLabels, rowCount) { rowIndex ->
            List(columnCount) { columnIndex ->
                action.invoke(rowIndex, columnIndex, rows[rowIndex][columnIndex])
            }
        }
    }

    operator fun get(rowIndex: Int): List<Item> {
        return rows[rowIndex]
    }

    operator fun set(rowIndex: Int, list: List<Item>) {
        assert(list.size == columnCount)
        rows[rowIndex] = list
    }

    override fun hashCode(): Int {
        var result = columnCount
        result = 31 * result + columnLabels.hashCode()
        result = 31 * result + rowCount
        result = 31 * result + rows.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TableContent<*>

        if (columnCount != other.columnCount) return false
        if (columnLabels != other.columnLabels) return false
        if (rowCount != other.rowCount) return false
        if (rows != other.rows) return false

        return true
    }

    private fun addRow(row: List<Item>) {
        rows.add(row)
    }

    companion object {
        fun <T> empty(): TableContent<T> {
            return TableContent(0)
        }
    }
}