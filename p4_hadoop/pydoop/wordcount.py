def mapper(key, text, writer):
    for word in text.split():
        writer.emit(word, "1")

def reducer(word, icounts, writer):
    writer.emit(word, sum(map(int, icounts)))
