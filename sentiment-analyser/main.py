
import thriftpy
from thriftpy.rpc import make_server

from flair.models import TextClassifier
from flair.data import Sentence

sv_thrift = thriftpy.load('service.thrift', module_name='sv_thrift')
classifier = TextClassifier.load('en-sentiment')

class Dispatcher:
    def getSentiment(self, text):
        sentence = Sentence(text)
        classifier.predict(sentence)
        # label is either POSITIVE or NEGATIVE for value property and double for score property
        label = sentence.labels[0]
        # set score to <-1,0) for NEGATIVE and (0,1> for POSITIVE
        label_score = (1 if label.value == "POSITIVE" else -1)*(label.score)
        # standardize values to be <0,0.5) for NEGATIVE and (0.5,1> for POSITIVE
        return ((label_score + 1)/2)

ip = '127.0.0.1'
port = 3000
server = make_server(sv_thrift.TextSentiment, Dispatcher(), ip, port)
server.serve()