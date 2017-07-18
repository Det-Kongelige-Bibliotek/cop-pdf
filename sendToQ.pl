#!/usr/bin/perl
use strict;
use warnings;
use lib 'lib';

use Net::Stomp;
my $host = 'cop-02.kb.dk';
# my $host = 'disdev-01.kb.dk';

my $stomp = Net::Stomp->new({ hostname => $host, port => '61613' });
$stomp->connect({ login => 'admin', passcode => 'admin' } );


while(my $line = <>) {
    next unless $line =~ m/^http/;
    chomp $line;
    warn "$line\n";
    $stomp->send(
        {   destination => '/queue/kb.dataloader.cop.pdf',
            body        => $line,
	    time        => time(),
        }
    );
}

$stomp->disconnect;
