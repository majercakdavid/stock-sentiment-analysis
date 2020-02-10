import thriftpy
from thriftpy.rpc import make_client

sv_thrift = thriftpy.load('service.thrift', module_name='sv_thrift')

ip = '127.0.0.1'
port = 3000
client = make_client(sv_thrift.TextSentiment, ip, port)

# Call Target Function
z = client.getSentiment("This is very good")
print(z)
client.close()