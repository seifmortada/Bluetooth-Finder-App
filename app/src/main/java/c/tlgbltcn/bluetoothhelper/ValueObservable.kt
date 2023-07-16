package c.tlgbltcn.bluetoothhelper

class ValueObservable<T> {
    private val observers = mutableListOf<(T) -> Unit>()
    private var value: T? = null

    fun addObserver(observer: (T) -> Unit) {
        observers.add(observer)
        value?.let(observer)
    }

    fun removeObserver(observer: (T) -> Unit) {
        observers.remove(observer)
    }

    fun setValue(value: T) {
        this.value = value
        observers.forEach { observer ->
            observer(value)
        }
    }
}
