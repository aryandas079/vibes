import urllib.request, urllib.parse, json

# Search Deezer for ISRC
url = "https://api.deezer.com/search?q=Sabrina%20Carpenter%20Taste"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode('utf-8'))
    track = data['data'][0]
    print('Deezer track:', track['title'], 'id:', track['id'])
    tid = track['id']

track_url = f"https://api.deezer.com/track/{tid}"
with urllib.request.urlopen(urllib.request.Request(track_url, headers={'User-Agent': 'Mozilla/5.0'})) as dresp:
    ddata = json.loads(dresp.read().decode('utf-8'))
    isrc = ddata.get('isrc')
    print('ISRC:', isrc)
