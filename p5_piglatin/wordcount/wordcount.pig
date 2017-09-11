A = load './TheCompleteSherlockHolmes.txt' as line:chararray;
B = foreach A generate flatten(TOKENIZE(line)) as word;
C = group B by word;
D = foreach C generate $0, COUNT($1);
store D into './wordcount';